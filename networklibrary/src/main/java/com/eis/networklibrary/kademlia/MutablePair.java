package com.eis.networklibrary.kademlia;

/**
 * Since java does not provide mutable pairs, we define our own trivial implementation.
 *
 * @param <F>
 * @param <S>
 */
class MutablePair<F, S> {
    F first;
    S second;

    MutablePair(F f, S s) {
        first = f;
        second = s;
    }
}