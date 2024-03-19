/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.helper.menu.scheme;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.*;

import javax.annotation.Nullable;

/**
 * Helps to populate a menu with border items
 */
@NonnullByDefault
public class MenuScheme {
    private static final boolean[] EMPTY_MASK = new boolean[]{false, false, false, false, false, false, false, false, false};
    private static final int[] EMPTY_SCHEME = new int[0];

    private final SchemeMapping mapping;
    private final List<boolean[]> maskRows;
    private final List<int[]> schemeRows;

    public MenuScheme(@Nullable SchemeMapping mapping) {
        this.mapping = mapping == null ? StandardSchemeMappings.EMPTY : mapping;
        this.maskRows = new ArrayList<>();
        this.schemeRows = new ArrayList<>();
    }

    public MenuScheme() {
        this((SchemeMapping) null);
    }

    private MenuScheme(MenuScheme other) {
        this.mapping = other.mapping.copy();
        this.maskRows = new ArrayList<>();
        for (boolean[] arr : other.maskRows) {
            this.maskRows.add(Arrays.copyOf(arr, arr.length));
        }
        this.schemeRows = new ArrayList<>();
        for (int[] arr : other.schemeRows) {
            this.schemeRows.add(Arrays.copyOf(arr, arr.length));
        }
    }

    public MenuScheme mask(String s) {
        char[] chars = s.replace(" ", "").toCharArray();
        if (chars.length != 9) {
            throw new IllegalArgumentException("invalid mask: " + s);
        }
        boolean[] ret = new boolean[9];
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '1' || c == 't') {
                ret[i] = true;
            } else if (c == '0' || c == 'f' || c == 'x') {
                ret[i] = false;
            } else {
                throw new IllegalArgumentException("invalid mask character: " + c);
            }
        }
        this.maskRows.add(ret);
        return this;
    }

    public MenuScheme masks(String... strings) {
        for (String s : strings) {
            mask(s);
        }
        return this;
    }

    public MenuScheme masks(boolean slot0) {
        return masks(slot0, false);
    }

    public MenuScheme masks(boolean slot0, boolean slot1) {
        return masks(slot0, slot1, false);
    }

    public MenuScheme masks(boolean slot0, boolean slot1, boolean slot2) {
        return masks(slot0, slot1, slot2, false);
    }

    public MenuScheme masks(boolean slot0, boolean slot1, boolean slot2, boolean slot3) {
        return masks(slot0, slot1, slot2, slot3, false);
    }

    public MenuScheme masks(boolean slot0, boolean slot1, boolean slot2, boolean slot3, boolean slot4) {
        return masks(slot0, slot1, slot2, slot3, slot4, false);
    }

    public MenuScheme masks(boolean slot0, boolean slot1, boolean slot2, boolean slot3, boolean slot4, boolean slot5) {
        return masks(slot0, slot1, slot2, slot3, slot4, slot5, false);
    }

    public MenuScheme masks(boolean slot0, boolean slot1, boolean slot2, boolean slot3, boolean slot4, boolean slot5, boolean slot6) {
        return masks(slot0, slot1, slot2, slot3, slot4, slot5, slot6, false);
    }

    public MenuScheme masks(boolean slot0, boolean slot1, boolean slot2, boolean slot3, boolean slot4, boolean slot5, boolean slot6, boolean slot7) {
        return masks(slot0, slot1, slot2, slot3, slot4, slot5, slot6, slot7, false);
    }

    public MenuScheme masks(boolean slot0, boolean slot1, boolean slot2, boolean slot3, boolean slot4, boolean slot5, boolean slot6, boolean slot7, boolean slot8) {
        boolean[] ret = new boolean[9];
        ret[0] = slot0;
        ret[1] = slot1;
        ret[2] = slot2;
        ret[3] = slot3;
        ret[4] = slot4;
        ret[5] = slot5;
        ret[6] = slot6;
        ret[7] = slot7;
        ret[8] = slot8;
        this.maskRows.add(ret);
        return this;
    }

    public MenuScheme masks(boolean ... slots) {
        // split into groups of 9
        if (slots.length <= 9) {
            this.maskRows.add(Arrays.copyOf(slots, 9));
        } else {
            for (int i = 0; i < slots.length; i += 9) {
                this.maskRows.add(Arrays.copyOfRange(slots, i, Math.min(i + 9, slots.length)));
            }
        }
        return this;
    }

    public MenuScheme maskEmpty(int lines) {
        for (int i = 0; i < lines; i++) {
            this.maskRows.add(EMPTY_MASK);
            this.schemeRows.add(EMPTY_SCHEME);
        }
        return this;
    }

    public MenuScheme scheme(int... schemeIds) {
        for (int schemeId : schemeIds) {
            if (!this.mapping.hasMappingFor(schemeId)) {
                throw new IllegalArgumentException("mapping does not contain value for id: " + schemeId);
            }
        }
        this.schemeRows.add(schemeIds);
        return this;
    }

    public void apply(Gui gui) {
        // the index of the item slot in the inventory
        int invIndex = 0;

        // iterate all of the loaded masks
        for (int i = 0; i < this.maskRows.size(); i++) {
            boolean[] mask = this.maskRows.get(i);
            int[] scheme = this.schemeRows.get(i);

            int schemeIndex = 0;

            // iterate the values in the mask (0 --> 8)
            for (boolean b : mask) {

                // increment the index in the gui. we're handling a new item.
                int index = invIndex++;

                // if this index is masked.
                if (b) {

                    // this is the value from the scheme map for this slot.
                    int schemeMappingId = scheme[schemeIndex++];

                    // lookup the value for this location, and apply it to the gui
                    this.mapping.get(schemeMappingId).ifPresent(item -> gui.setItem(index, item));
                }
            }
        }
    }

    public List<Integer> getMaskedIndexes() {
        List<Integer> ret = new LinkedList<>();

        // the index of the item slot in the inventory
        int invIndex = 0;

        // iterate all of the loaded masks
        for (boolean[] mask : this.maskRows) {
            // iterate the values in the mask (0 --> 8)
            for (boolean b : mask) {

                // increment the index in the gui. we're handling a new item.
                int index = invIndex++;

                // if this index is masked.
                if (b) {
                    ret.add(index);
                }
            }
        }

        return ret;
    }

    public ImmutableList<Integer> getMaskedIndexesImmutable() {
        return ImmutableList.copyOf(getMaskedIndexes());
    }

    public MenuPopulator newPopulator(Gui gui) {
        return new MenuPopulator(gui, this);
    }

    public MenuScheme copy() {
        return new MenuScheme(this);
    }
}
