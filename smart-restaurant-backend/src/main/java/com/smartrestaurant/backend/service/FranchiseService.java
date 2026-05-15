package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.FranchiseRegistrationRequest;
import com.smartrestaurant.backend.dto.FranchiseResponse;

public interface FranchiseService {
    FranchiseResponse registerFranchise(FranchiseRegistrationRequest request);
}
