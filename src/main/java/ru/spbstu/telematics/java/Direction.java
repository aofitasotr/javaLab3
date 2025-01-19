package ru.spbstu.telematics.java;

public enum Direction {

    NS, // North-South
    SN, // South-North
    WE, // West-East
    EW, // East-West
    ES; // East-South

    public boolean doesNotIntersect(Direction other) {
        switch (this) {
            case NS:
                return (other == SN || other == ES);
            case SN:
                return (other == NS);
            case WE:
                return (other == EW);
            case EW:
                return (other == WE || other == ES);
            case ES:
                return (other == NS || other == EW);
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
