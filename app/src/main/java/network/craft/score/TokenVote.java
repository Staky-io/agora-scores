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

import score.ObjectReader;
import score.ObjectWriter;

import java.math.BigInteger;

public class TokenVote {
    private final String vote;
    private final BigInteger amount;

    public TokenVote(String vote, BigInteger amount) {
        this.vote = vote;
        this.amount = amount;
    }

    public String getVote() {
        return vote;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public static void writeObject(ObjectWriter w, TokenVote v) {
        w.writeListOf(v.vote, v.amount);
    }

    public static TokenVote readObject(ObjectReader r) {
        r.beginList();
        TokenVote v = new TokenVote(
                r.readString(),
                r.readBigInteger()
        );
        r.end();
        return v;
    }
}
