package neresources.api.restriction;

/** How a restriction treats the values it holds. */
public enum RestrictionType {
    /** No restriction; every value passes. */
    NONE,
    /** Every value except the listed ones passes. */
    BLACKLIST,
    /** Only the listed values pass. */
    WHITELIST
}
