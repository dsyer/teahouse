package org.example.teahouse.water.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.teahouse.water.api.CreateWaterRequest;
import org.example.teahouse.water.api.WaterResponse;
import org.example.teahouse.water.repo.Water;
import org.example.teahouse.water.repo.WaterRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@Api(tags = "Water API")
public class WaterController {
    private final WaterRepository waterRepository;

    @GetMapping("/water")
    @ApiOperation("Fetches all of the resources")
    public Iterable<WaterResponse> findAll() {
        return StreamSupport.stream(waterRepository.findAll().spliterator(), false)
            .map(Water::toWaterResponse)
            .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping("/water/{id}")
    @ApiOperation("Fetches a resource by its ID")
    public WaterResponse findById(@PathVariable Long id) {
        return waterRepository.findById(id)
            .map(Water::toWaterResponse)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No resource found with that id"));
    }

    @GetMapping("/water/search/findBySize")
    @ApiOperation("Finds a resource by its size")
    public WaterResponse findBySize(@RequestParam("size") String size) {
        return waterRepository.findBySize(size)
            .map(Water::toWaterResponse)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No resource found with that size"));
    }

    @PostMapping("/water")
    @ResponseStatus(CREATED)
    @ApiOperation("Creates a resource")
    public WaterResponse save(@Valid @RequestBody CreateWaterRequest createWaterRequest) {
        return waterRepository.save(Water.fromCreateWaterRequest(createWaterRequest)).toWaterResponse();
    }

    @DeleteMapping("/water")
    @ResponseStatus(NO_CONTENT)
    @ApiOperation("Deletes all of the resources")
    public void deleteAll() {
        waterRepository.deleteAll();
    }

    @DeleteMapping("/water/{id}")
    @ResponseStatus(NO_CONTENT)
    @ApiOperation("Deletes a resource by its ID ")
    public void deleteById(@PathVariable Long id) {
        waterRepository.deleteById(id);
    }
}
