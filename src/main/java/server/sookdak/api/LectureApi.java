package server.sookdak.api;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.sookdak.dto.res.lecture.LectureResponse;
import server.sookdak.dto.res.lecture.LectureResponseDto;
import server.sookdak.service.LectureService;

import static server.sookdak.constants.SuccessCode.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/lecture")
public class LectureApi {

    private final LectureService lectureService;

    @GetMapping("/{page}")
    public ResponseEntity<LectureResponse> getLectures(@PathVariable int page) {
        LectureResponseDto response = lectureService.getLectures(page);

        return LectureResponse.newResponse(LECTURE_READ_SUCCESS, response);
    }

    @PostMapping("/crawl")
    public void crawling() throws InterruptedException {
        lectureService.crawling();
    }
}