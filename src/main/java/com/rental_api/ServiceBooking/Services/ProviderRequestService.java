package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.ProviderRequestDto;
import com.rental_api.ServiceBooking.Dto.Response.ProviderRequestResponse;

import java.util.List;

public interface ProviderRequestService {

    ProviderRequestResponse createRequest(ProviderRequestDto dto, String email);

    List<ProviderRequestResponse> getAllRequests();

    ProviderRequestResponse approveRequest(Long requestId);

    ProviderRequestResponse rejectRequest(Long requestId);
}
