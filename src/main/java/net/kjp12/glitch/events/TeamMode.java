/*
 * Copyright (c) 2021 KJP12
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * */
package net.kjp12.glitch.events;// Created 2021-03-20T20:07:07

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static net.kjp12.glitch.events.Main.mkMapSplitRemainders;
import static net.kjp12.glitch.events.Main.mkMapWithRemainders;

/**
 * @author KJP12
 * @since 0.0.0
 */
public enum TeamMode {
    SPLIT_BY_N {
        @Override
        public <T> Function<Set<T>, Map<String, Set<T>>> mkFunction(int teams) {
            return pl -> {
                int t = pl.size();
                return mkMapWithRemainders(pl, t / teams, teams, t % teams);
            };
        }
    },
    SPLIT_BY_N_NO_REMAINDER {
        @Override
        public <T> Function<Set<T>, Map<String, Set<T>>> mkFunction(int teams) {
            return pl -> {
                int t = pl.size();
                return mkMapSplitRemainders(pl, t / teams, teams, t % teams);
            };
        }
    },
    SPLIT_INTO_N {
        @Override
        public <T> Function<Set<T>, Map<String, Set<T>>> mkFunction(int teams) {
            return pl -> {
                //var m = new HashMap<String, Set<ServerPlayerEntity>>((int) (teams * 0.75F) + 1, 0.75F);
                int t = pl.size(); // total size
                return mkMapWithRemainders(pl, teams, t / teams, t % teams);
                //int l = t / teams; // size of list
                //int r = t % teams; // remaining to split
                //var itr = pl.iterator();
                //for(int i = 0, k = 0; i < teams && itr.hasNext(); i++) {
                //    var h = new HashSet<ServerPlayerEntity>((int)(l * 0.75F) + 1, 0.75F);
                //    for(int j = 0; j < l; j++) {
                //        h.add(itr.next());
                //    }
                //    if(k++ < r) {
                //        h.add(itr.next());
                //    }
                //    m.put(String.valueOf(i), h);
                //}
                //m.values().removeIf(Set::isEmpty);
                //return m;
            };
        }
    },
    SPLIT_INTO_N_NO_REMAINDER {
        @Override
        public <T> Function<Set<T>, Map<String, Set<T>>> mkFunction(int teams) {
            return pl -> {
                //var m = new HashMap<String, Set<ServerPlayerEntity>>((int) (teams * 0.75F) + 1, 0.75F);
                int t = pl.size(); // total size
                return mkMapSplitRemainders(pl, teams, t / teams, t % teams);
                //int l = t / teams; // size of list
                //int r = t % teams; // remaining to split
                //var itr = pl.iterator();
                //for(int i = 0, k = 0; i < teams && itr.hasNext(); i++) {
                //    var h = new HashSet<ServerPlayerEntity>((int)(l * 0.75F) + 1, 0.75F);
                //    for(int j = 0; j < l; j++) {
                //        h.add(itr.next());
                //    }
                //    m.put(String.valueOf(i), h);
                //}
                //if(r != 0) {
                //    var h = new HashSet<ServerPlayerEntity>((int) (r * 0.75F) + 1, 0.75F);
                //    while (itr.hasNext()) {
                //        h.add(itr.next());
                //    }
                //    // null == spectator
                //    m.put(null, h);
                //}
                //m.values().removeIf(Set::isEmpty);
                //return m;
            };
        }
    };

    public abstract <T> Function<Set<T>, Map<String, Set<T>>> mkFunction(int teams);
}
