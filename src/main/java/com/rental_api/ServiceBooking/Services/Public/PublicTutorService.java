package com.rental_api.ServiceBooking.Services.Public;

import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;
import java.util.List;

public interface PublicTutorService {
    List<TutorCardResponse> getAllPublicTutors();
}