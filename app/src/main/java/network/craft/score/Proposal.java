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
import score.ObjectReader;
import score.ObjectWriter;

public class Proposal {
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_CLOSED = 2;
    public static final int STATUS_CANCELED = 3;
    public static final String[] STATUS_MSG = new String[]{
            "None",
            "Active",
            "Closed",
            "Canceled"
    };

    private final Address creator;
    private final long startTime;
    private final long endTime;
    private final String ipfsHash;
    private int status;

    public Proposal(Address creator, long startTime, long endTime, String ipfsHash, int status) {
        this.creator = creator;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ipfsHash = ipfsHash;
        this.status = status;
    }

    public Address getCreator() {
        return creator;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getIpfsHash() {
        return ipfsHash;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static void writeObject(ObjectWriter w, Proposal p) {
        w.writeListOf(p.creator, p.startTime, p.endTime, p.ipfsHash, p.status);
    }

    public static Proposal readObject(ObjectReader r) {
        r.beginList();
        Proposal p = new Proposal(
                r.readAddress(),
                r.readLong(),
                r.readLong(),
                r.readString(),
                r.readInt()
        );
        r.end();
        return p;
    }
}
