export class FilterRuleTypes {
    public static readonly TopRows = 'TopRows';
    public static readonly Range  = 'RangeRows';
    public static readonly RegularInterval = 'TopRowsAtRegularInterval';
    public static readonly IsMissing = 'Missing';
    public static readonly EqualTo = 'Exactly';
    public static readonly NotEqualTo = 'NotEqualTo';
    public static readonly GreaterThanOrEqualTo = 'GreaterThanOrEqualTo';
    public static readonly LessThanOrEqualTo = 'LessThanOrEqualTo';
    public static readonly Contains = 'Contains';
    public static readonly StartsWith = 'StartsWith';
    public static readonly EndsWith = 'EndsWith';
    public static readonly IsBetween = 'Between';
    public static readonly IsOneOf = 'ValueIsOneOf';
    public static readonly TypeMismatched = 'typeMismatched';
}

export class FilterRuleActionTypes {
    public static readonly Delete = 'Delete';
    public static readonly Keep = 'Keep';
}
