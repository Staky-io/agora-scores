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

import java.math.BigInteger;
import java.util.Map;

public class AgoraImpl implements AgoraGov {
    public AgoraImpl() {
    }

    @External
    public void setGovernanceToken(Address _address, String _type, BigInteger _id) {

    }

    @External
    public void setMinimumThreshold(BigInteger _amount) {

    }

    @External
    public void submitProposal(BigInteger _endTime, String _ipfsHash) {

    }

    @External
    public void vote(BigInteger _proposalId, String _vote) {

    }

    @External
    public void cancelProposal(BigInteger _proposalId) {

    }

    @External
    public void closeProposal(BigInteger _proposalId) {

    }

    @External(readonly=true)
    public Map<String, Object> getProposal(BigInteger _proposalId) {
        return Map.of();
    }

    @EventLog(indexed=1)
    public void ProposalSubmitted(BigInteger _proposalId, Address _creator) {}

    @EventLog(indexed=1)
    public void ProposalCanceled(BigInteger _proposalId) {}

    @EventLog(indexed=1)
    public void ProposalClosed(BigInteger _proposalId) {}
}
