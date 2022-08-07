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
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;
import java.util.Map;

public interface AgoraGov {
    /**
     * Sets the token contract address and type to be able to call the correct method to read balances.
     * Admin-only method.
     *
     * @param _address the address of the token contract to query
     * @param _type IRC-2 or IRC-31
     * @param _id (Optional) If the type is IRC-31, specify a pool id to query
     */
    @External
    void setGovernanceToken(Address _address, String _type, @Optional BigInteger _id);

    /**
     * Sets the minimum required of tokens to submit a new proposal.
     * Admin-only method.
     *
     * @param _amount the minimum amount of token to have in `tokenAddress`
     */
    @External
    void setMinimumThreshold(BigInteger _amount);

    /**
     * Creates a proposal.
     * The creator must meet the `minimumThreshold` to submit a governance proposal.
     * The proposal id is automatically incremented.
     * The proposal has 3 status: `active`, `closed`, and `cancelled`.
     *
     * @param _endTime the timestamp of the end of the vote and must be minimum 1 day from `now` and maximum 7 days
     * @param _ipfsHash the hash of the content of the proposal, formatting is handled by the frontend
     */
    @External
    void submitProposal(BigInteger _endTime, String _ipfsHash);

    /**
     * Votes for a proposal.
     * All the voting power (got by using balancedOf against `tokenAddress`) is accounted for the _vote.
     * The proposal has to be in `active` state.
     *
     * @param _proposalId id of the proposal
     * @param _vote can be either for, against or abstain
     */
    @External
    void vote(BigInteger _proposalId, String _vote);

    /**
     * Cancels a proposal.
     * Possible only by the creator and if the proposal has been created in the past 3 hours.
     * The proposal has to be in `active` state.
     *
     * @param _proposalId id of the proposal
     */
    @External
    void cancelProposal(BigInteger _proposalId);

    /**
     * Closes a proposal.
     * Anyone can call it, the _endTime has to be reached.
     * The proposal has to be in `active` state.
     *
     * @param _proposalId id of the proposal
     */
    @External
    void closeProposal(BigInteger _proposalId);

    /**
     * Returns the object of the proposal.
     *
     * @param _proposalId id of the proposal
     * @return Map of proposalId, state, forVoices, againstVoices, abstainVoices, endTime, creator
     */
    @External(readonly=true)
    Map<String, Object> getProposal(BigInteger _proposalId);

    /**
     * Notifies the user that the proposal has been successfully submitted.
     *
     * @param _proposalId id of the proposal
     * @param _creator the proposal submitter
     */
    @EventLog(indexed=1)
    void ProposalSubmitted(BigInteger _proposalId, Address _creator);

    /**
     * Notifies the user that the proposal has been canceled.
     *
     * @param _proposalId id of the proposal
     */
    @EventLog(indexed=1)
    void ProposalCanceled(BigInteger _proposalId);

    /**
     * Notifies the user that the proposal has been closed.
     *
     * @param _proposalId id of the proposal
     */
    @EventLog(indexed=1)
    void ProposalClosed(BigInteger _proposalId);
}
