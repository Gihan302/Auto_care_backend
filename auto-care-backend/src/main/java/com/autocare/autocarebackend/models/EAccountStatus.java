package com.autocare.autocarebackend.models;

public enum EAccountStatus {
    PENDING,
    APPROVED,  // Change from ACTIVE to APPROVED
    REJECTED
    // Remove ACTIVE since your database doesn't use it
}