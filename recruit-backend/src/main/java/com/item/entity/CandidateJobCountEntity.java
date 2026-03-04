package com.item.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class CandidateJobCountEntity implements Serializable {

    private Long Applications;

    private Long screened;

    private Long AIVetted;

    private Long pendingReview;

    private Long ready;

    private Long backgroundChecked;

    private Long denied;

}