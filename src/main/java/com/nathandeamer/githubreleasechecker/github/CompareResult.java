package com.nathandeamer.githubreleasechecker.github;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompareResult {

    private final String name;
    private final int by;
    private final String link;

}
