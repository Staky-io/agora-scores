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

public class Proposal {
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_CLOSED = 2;
    public static final int STATUS_CANCELED = 3;

    private final BigInteger endTime;
    private final String ipfsHash;
    private final int status;

    public Proposal(BigInteger endTime, String ipfsHash, int status) {
        this.endTime = endTime;
        this.ipfsHash = ipfsHash;
        this.status = status;
    }

    public BigInteger getEndTime() {
        return endTime;
    }

    public String getIpfsHash() {
        return ipfsHash;
    }

    public static void writeObject(ObjectWriter w, Proposal p) {
        w.writeListOf(p.endTime, p.ipfsHash, p.status);
    }

    public static Proposal readObject(ObjectReader r) {
        r.beginList();
        Proposal p = new Proposal(
                r.readBigInteger(),
                r.readString(),
                r.readInt()
        );
        r.end();
        return p;
    }
}
