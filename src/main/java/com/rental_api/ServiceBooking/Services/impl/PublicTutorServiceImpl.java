package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;
import com.rental_api.ServiceBooking.Entity.Subject;
import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Repository.TutorRepository;
import com.rental_api.ServiceBooking.Services.Public.PublicTutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicTutorServiceImpl implements PublicTutorService {

    private final TutorRepository tutorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TutorCardResponse> getAllPublicTutors() {
        return tutorRepository.findAll().stream()
                .filter(Tutor::isPublic)
                .map(tutor -> {
                    List<String> subjects = tutor.getOpenClasses().stream()
                            .flatMap(c -> c.getSubjects().stream())
                            .map(Subject::getName)
                            .distinct()
                            .collect(Collectors.toList());

                    String location = tutor.getOpenClasses().isEmpty() ? "" :
                            tutor.getOpenClasses().get(0).getLocation().getDistrict() + ", " +
                            tutor.getOpenClasses().get(0).getLocation().getCity();

                    String profileImage = (tutor.getMedia() != null) ? tutor.getMedia().getProfileImageUrl() : null;

                    return TutorCardResponse.builder()
                            .tutorId(tutor.getId())
                            .fullname(tutor.getUser().getFullname())
                            .profilePicture(profileImage)
                            .rating(tutor.getAverageRating())
                            .studentsTaught(tutor.getTotalStudentsTaught())
                            .bio(tutor.getBio())
                            .subjects(subjects)
                            .location(location)
                            .totalOpenClasses(tutor.getOpenClasses().size())
                            .build();
                })
                .collect(Collectors.toList());
    }
}