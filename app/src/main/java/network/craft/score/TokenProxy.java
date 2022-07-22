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
import score.Context;

import java.math.BigInteger;

public class TokenProxy {
    public static final String IRC2 = "irc-2";
    public static final String IRC31 = "irc-31";

    private final Address address;
    private final String type;
    private final BigInteger id;

    public TokenProxy(Address address, String type, BigInteger id) {
        Context.require(address != null, "TokenAddressNotSet");
        this.address = address;
        this.type = type;
        this.id = id;
    }

    public BigInteger balanceOf(Address holder) {
        if (IRC2.equals(type)) {
            return Context.call(BigInteger.class, address, "balanceOf", holder);
        } else {
            return Context.call(BigInteger.class, address, "balanceOf", holder, id);
        }
    }
}
