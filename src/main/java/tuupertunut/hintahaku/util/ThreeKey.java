/*
 * The MIT License
 *
 * Copyright 2016 Tuupertunut.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tuupertunut.hintahaku.util;

import java.util.Objects;

/**
 *
 * @author Tuupertunut
 */
public class ThreeKey<K1, K2, K3> {

    private final K1 key1;
    private final K2 key2;
    private final K3 key3;

    public ThreeKey(K1 key1, K2 key2, K3 key3) {
        this.key1 = key1;
        this.key2 = key2;
        this.key3 = key3;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.key1);
        hash = 71 * hash + Objects.hashCode(this.key2);
        hash = 71 * hash + Objects.hashCode(this.key3);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ThreeKey<?, ?, ?> other = (ThreeKey<?, ?, ?>) obj;
        if (!Objects.equals(this.key1, other.key1)) {
            return false;
        }
        if (!Objects.equals(this.key2, other.key2)) {
            return false;
        }
        if (!Objects.equals(this.key3, other.key3)) {
            return false;
        }
        return true;
    }
}
