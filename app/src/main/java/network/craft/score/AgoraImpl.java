/*
 * Copyright 2022 Craft Network
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package network.craft.score;

import score.Address;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;
import java.util.Map;

public class AgoraImpl implements AgoraGov {
    public static final BigInteger HOUR_IN_SECONDS = BigInteger.valueOf(3600);
    public static final BigInteger DAY_IN_SECONDS = HOUR_IN_SECONDS.multiply(BigInteger.valueOf(24));
    public static final BigInteger HOUR_IN_MICROSECONDS = HOUR_IN_SECONDS.multiply(BigInteger.valueOf(1_000_000));
    public static final BigInteger DAY_IN_MICROSECONDS = DAY_IN_SECONDS.multiply(BigInteger.valueOf(1_000_000));

    private final VarDB<Address> tokenAddress = Context.newVarDB("token_address", Address.class);
    private final VarDB<String> tokenType = Context.newVarDB("token_type", String.class);
    private final VarDB<BigInteger> tokenId = Context.newVarDB("token_id", BigInteger.class);
    private final VarDB<BigInteger> minimumThreshold = Context.newVarDB("minimum_threshold", BigInteger.class);

    private final VarDB<BigInteger> proposalId = Context.newVarDB("proposal_id", BigInteger.class);
    private final DictDB<BigInteger, Proposal> proposals = Context.newDictDB("proposals", Proposal.class);
    // proposalId => holder => token votes
    private final BranchDB<BigInteger, DictDB<Address, TokenVote>> tokenVotes = Context.newBranchDB("token_votes", TokenVote.class);
    private final DictDB<BigInteger, Votes> votes = Context.newDictDB("votes_sum", Votes.class);

    @External(readonly=true)
    public String name() {
        return "AgoraScore";
    }

    private void checkCallerOrThrow(Address caller, String errMsg) {
        Context.require(Context.getCaller().equals(caller), errMsg);
    }

    private void onlyOwner() {
        checkCallerOrThrow(Context.getOwner(), "OnlyOwner");
    }

    @External(readonly=true)
    public Map<String, Object> governanceTokenInfo() {
        var type = tokenType();
        if (type == null) {
            return Map.of();
        }
        if (TokenProxy.IRC2.equals(type)) {
            return Map.of(
                    "_address", tokenAddress(),
                    "_type", type);
        } else {
            return Map.of(
                    "_address", tokenAddress(),
                    "_type", type,
                    "_id", tokenId());
        }
    }

    private Address tokenAddress() {
        return tokenAddress.get();
    }

    private String tokenType() {
        return tokenType.get();
    }

    private BigInteger tokenId() {
        return tokenId.getOrDefault(BigInteger.ZERO);
    }

    @External
    public void setGovernanceToken(Address _address, String _type, @Optional BigInteger _id) {
        onlyOwner();
        Context.require(tokenAddress() == null, "GovernanceTokenAlreadySet");
        var type = _type.toLowerCase();
        switch (type) {
            case TokenProxy.IRC2:
            case TokenProxy.IRC31:
                tokenType.set(type);
                break;
            default:
                Context.revert("InvalidTokenType");
        }
        tokenAddress.set(_address);
        if (TokenProxy.IRC31.equals(type)) {
            tokenId.set(_id);
        }
    }

    @External(readonly=true)
    public BigInteger minimumThreshold() {
        return minimumThreshold.getOrDefault(BigInteger.ZERO);
    }

    @External
    public void setMinimumThreshold(BigInteger _amount) {
        onlyOwner();
        Context.require(_amount.signum() > 0, "Minimum threshold must be positive");
        minimumThreshold.set(_amount);
    }

    @External(readonly=true)
    public BigInteger lastProposalId() {
        return proposalId.getOrDefault(BigInteger.ZERO);
    }

    private void checkEndTimeOrThrow(BigInteger _endTime) {
        var now = Context.getBlockTimestamp();
        var minimumEnd = DAY_IN_MICROSECONDS.longValue();
        Context.require(_endTime.longValue() > now + minimumEnd, "InvalidEndTime");
    }

    private BigInteger getNextId() {
        BigInteger _id = lastProposalId();
        _id = _id.add(BigInteger.ONE);
        proposalId.set(_id);
        return _id;
    }

    @External
    public void submitProposal(BigInteger _endTime, String _ipfsHash) {
        Address sender = Context.getCaller();
        Context.require(!sender.isContract(), "Only EOA can submit proposal");
        checkEndTimeOrThrow(_endTime);

        var tokenProxy = new TokenProxy(tokenAddress(), tokenType(), tokenId());
        var balance = tokenProxy.balanceOf(sender);
        Context.require(minimumThreshold().compareTo(balance) <= 0, "MinimumThresholdNotMet");

        BigInteger pid = getNextId();
        long createTime = Context.getBlockTimestamp();
        long endTime = _endTime.longValue();
        Proposal pl = new Proposal(sender, createTime, endTime, _ipfsHash, Proposal.STATUS_ACTIVE);
        proposals.set(pid, pl);
        ProposalSubmitted(pid, sender);
    }

    @External
    public void vote(BigInteger _proposalId, String _vote) {
        Address sender = Context.getCaller();
        Context.require(!sender.isContract(), "Only EOA can submit proposal");

        Proposal pl = proposals.get(_proposalId);
        Context.require(pl != null, "InvalidProposalId");
        Context.require(pl.getStatus() == Proposal.STATUS_ACTIVE, "ProposalNotActive");

        var tokenProxy = new TokenProxy(tokenAddress(), tokenType(), tokenId());
        var balance = tokenProxy.balanceOf(sender);
        Context.require(balance.signum() > 0, "NotTokenHolder");

        var vote = _vote.toLowerCase();
        Context.require(Votes.isValid(vote), "InvalidVoteType");

        Context.require(tokenVotes.at(_proposalId).get(sender) == null, "AlreadyVoted");
        tokenVotes.at(_proposalId).set(sender, new TokenVote(vote, balance));
        var vs = votes.get(_proposalId);
        if (vs == null) {
            vs = new Votes();
        }
        vs.increase(vote, balance);
        votes.set(_proposalId, vs);
    }

    @External
    public void cancelProposal(BigInteger _proposalId) {
        Address sender = Context.getCaller();
        Proposal pl = proposals.get(_proposalId);
        Context.require(pl != null, "InvalidProposalId");
        Context.require(pl.getCreator().equals(sender), "NotCreator");
        Context.require(pl.getStatus() == Proposal.STATUS_ACTIVE, "ProposalNotActive");

        long now = Context.getBlockTimestamp();
        long graceTime = 3 * HOUR_IN_MICROSECONDS.longValue();
        Context.require(pl.getStartTime() + graceTime > now, "GraceTimePassed");

        pl.setStatus(Proposal.STATUS_CANCELED);
        proposals.set(_proposalId, pl);
        ProposalCanceled(_proposalId);
    }

    @External
    public void closeProposal(BigInteger _proposalId) {
        Proposal pl = proposals.get(_proposalId);
        Context.require(pl != null, "InvalidProposalId");
        Context.require(pl.getStatus() == Proposal.STATUS_ACTIVE, "ProposalNotActive");

        long now = Context.getBlockTimestamp();
        Context.require(pl.getEndTime() <= now, "EndTimeNotReached");

        pl.setStatus(Proposal.STATUS_CLOSED);
        proposals.set(_proposalId, pl);
        ProposalClosed(_proposalId);
    }

    @External(readonly=true)
    public Map<String, Object> getProposal(BigInteger _proposalId) {
        Proposal pl = proposals.get(_proposalId);
        Context.require(pl != null, "InvalidProposalId");

        var vs = votes.get(_proposalId);
        if (vs == null) {
            vs = new Votes();
        }
        return Map.ofEntries(
                Map.entry("_proposalId", _proposalId),
                Map.entry("_creator", pl.getCreator()),
                Map.entry("_status", Proposal.STATUS_MSG[pl.getStatus()]),
                Map.entry("_endTime", pl.getEndTime()),
                Map.entry("_startTime", pl.getStartTime()),
                Map.entry("_ipfsHash", pl.getIpfsHash()),
                Map.entry("_forVoices", vs.getFor()),
                Map.entry("_againstVoices", vs.getAgainst()),
                Map.entry("_abstainVoices", vs.getAbstain())
        );
    }

    @External(readonly=true)
    public Map<String, Object> getVote(Address _voter, BigInteger _proposalId) {
        var tokenVote = tokenVotes.at(_proposalId).get(_voter);
        if (tokenVote != null) {
            return Map.of(
                    "_vote", tokenVote.getVote(),
                    "_power", tokenVote.getAmount()
            );
        }
        return Map.of();
    }

    @EventLog(indexed=1)
    public void ProposalSubmitted(BigInteger _proposalId, Address _creator) {}

    @EventLog(indexed=1)
    public void ProposalCanceled(BigInteger _proposalId) {}

    @EventLog(indexed=1)
    public void ProposalClosed(BigInteger _proposalId) {}
}
