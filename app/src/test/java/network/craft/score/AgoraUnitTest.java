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

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import com.iconloop.score.token.irc2.IRC2Basic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import score.Context;

import java.math.BigInteger;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgoraUnitTest extends TestBase {
    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account alice = sm.createAccount();
    private Score tokenScore;
    private Score agoraScore;

    public static class IRC2TestToken extends IRC2Basic {
        public IRC2TestToken(BigInteger initialSupply) {
            super("TestToken", "TST", 18);
            _mint(Context.getCaller(), initialSupply);
        }
    }

    @BeforeEach
    void setup() throws Exception {
        tokenScore = sm.deploy(owner, IRC2TestToken.class, ICX.multiply(BigInteger.valueOf(1000)));
        agoraScore = sm.deploy(owner, AgoraImpl.class);
        // set governance token
        agoraScore.invoke(owner, "setGovernanceToken", tokenScore.getAddress(), "irc-2", BigInteger.ZERO);
        // transfer some token to Alice
        tokenScore.invoke(owner, "transfer", alice.getAddress(), ICX.multiply(BigInteger.valueOf(200)), "".getBytes());
    }

    @Test
    void name() {
        assertEquals("AgoraScore", agoraScore.call("name"));
    }

    @Test
    void getVote() {
        // submit dummy proposal
        long endTime = sm.getBlock().getTimestamp() + 2 * AgoraImpl.DAY_IN_MICROSECONDS.longValue();
        agoraScore.invoke(owner, "submitProposal", BigInteger.valueOf(endTime), "testIpfsHash");

        var pid = (BigInteger) agoraScore.call("lastProposalId");
        agoraScore.invoke(owner, "vote", pid, "for");
        agoraScore.invoke(alice, "vote", pid, "against");

        for (Account voter : new Account[]{owner, alice}) {
            @SuppressWarnings("unchecked")
            var vote = (Map<String, Object>) agoraScore.call("getVote", voter.getAddress(), pid);
            System.out.println(vote);
            if (voter.equals(owner)) {
                assertEquals("for", vote.get("_vote"));
            } else {
                assertEquals("against", vote.get("_vote"));
            }
            var balance = tokenScore.call("balanceOf", voter.getAddress());
            assertEquals(balance, vote.get("_power"));
        }
    }
}
