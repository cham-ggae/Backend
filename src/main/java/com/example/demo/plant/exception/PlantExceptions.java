package com.example.demo.plant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class PlantExceptions {

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class PlantNotCompletedException extends IllegalStateException {
        public PlantNotCompletedException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class RewardAlreadyClaimedException extends IllegalStateException {
        public RewardAlreadyClaimedException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class NotEnoughFamilyMembersException extends IllegalArgumentException {
        public NotEnoughFamilyMembersException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class UncompletedPlantExistsException extends IllegalStateException {
        public UncompletedPlantExistsException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class NutrientStockNotFoundException extends RuntimeException {
        public NutrientStockNotFoundException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class PointAlreadyAddedException extends RuntimeException {
        public PointAlreadyAddedException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class PlantNotFoundException extends RuntimeException {
        public PlantNotFoundException(String message) {
            super(message);
        }
    }
}
