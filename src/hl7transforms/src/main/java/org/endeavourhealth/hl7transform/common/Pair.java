package org.endeavourhealth.hl7transform.common;

import org.hl7.fhir.instance.model.valuesets.LocationPhysicalType;

import java.util.Objects;

    public final class Pair<F, S> {
    public final F first;
    public final S second;

        public Pair(F first, S second) {
                this.first = first;
                this.second = second;
            }

        @Override public int hashCode() {
                return Objects.hash(first, second);
            }

        @Override public boolean equals(Object o) {
                if (o == this)
                        return true;
                if (!(o instanceof Pair<?, ?>))
                        return false;

                    Pair<?, ?> other = (Pair<?, ?>) o;
                return Objects.equals(first, other.first)
                        && Objects.equals(second, other.second);
            }

        @Override public String toString() {
                return String.format("[%s = %s]", first, second);
            }

        public S getValue() {
            return this.second;
        }

        public F getKey() {
            return this.first;
        }
    }