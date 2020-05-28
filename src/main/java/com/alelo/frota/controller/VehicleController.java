package com.alelo.frota.controller;

import com.alelo.frota.model.Vehicle;
import com.alelo.frota.repository.VehicleRepository;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

    private List<String> errosRequest;

    @GetMapping("/page/{page}")
    public List<Vehicle> getVehiclesByPage(@PathVariable("page") int page) {

        return this.vehicleRepository.findAll(PageRequest.of(page, 10)).getContent();

    }

    @GetMapping("/count")
    public Long getCountVehicles() {

        return this.vehicleRepository.count();

    }

    @GetMapping("/{plate}")
    public ResponseEntity getVehicleByPlate(@PathVariable("plate") String plate) {

        List<Vehicle> vehicleList = this.vehicleRepository.findByPlate(plate);

        if (CollectionUtils.isEmpty(vehicleList)) {
            throw new NoSuchElementException("Vehicle not found for plate: " + plate);
        }

        return ResponseEntity.ok().body(vehicleList);
    }

    @PostMapping
    public ResponseEntity postVehicle(@RequestBody Vehicle vehicle) {

        ResponseEntity retorno;

        if (!this.validarRequestPostVehicle(vehicle)) {
            retorno = ResponseEntity.badRequest().body(this.errosRequest);
        } else {
            Vehicle vehicleSaved = this.vehicleRepository.save(vehicle);
            retorno = ResponseEntity.ok().body(vehicleSaved);
        }

        return retorno;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteVehicle(@PathVariable("id") Long id) {

        ResponseEntity retorno;

        Optional<Vehicle> oldVehicleOpt = this.vehicleRepository.findById(id);

        if(oldVehicleOpt.isPresent()){

            this.vehicleRepository.deleteById(id);
            retorno = new ResponseEntity<Vehicle>(HttpStatus.OK);

        } else {

            retorno =  new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }

        return retorno;

    }

    @PutMapping("/{id}")
    public ResponseEntity changeVehicle(@PathVariable("id") Long id, @RequestBody Vehicle vehicle) {

        ResponseEntity retorno;

        Optional<Vehicle> oldVehicleOpt = this.vehicleRepository.findById(id);

        if(oldVehicleOpt.isPresent()){

            Vehicle newVehicle = buildNewVehicle(vehicle, oldVehicleOpt);
            this.vehicleRepository.save(newVehicle);
            retorno = new ResponseEntity<Vehicle>(newVehicle, HttpStatus.OK);

        } else {

            retorno =  new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }

        return retorno;

    }

    private Vehicle buildNewVehicle(@RequestBody Vehicle vehicle, Optional<Vehicle> oldVehicleOpt) {
        Vehicle oldVehicle = oldVehicleOpt.get();
        oldVehicle.setPlate(vehicle.getPlate());
        oldVehicle.setModel(vehicle.getModel());
        oldVehicle.setManufacturer(vehicle.getManufacturer());
        oldVehicle.setColor(vehicle.getColor());
        oldVehicle.setActive(vehicle.getActive());
        return oldVehicle;
    }

    private Boolean validarRequestPostVehicle(Vehicle vehicle) {

        Boolean retorno = true;
        this.errosRequest = new ArrayList<>();

        if (vehicle == null) {

            this.errosRequest.add("Request without vehicle.");

        } else {

            if (Strings.isEmpty(vehicle.getPlate())) {
                this.errosRequest.add("Request without vehicle plate.");
            }

            if (Strings.isEmpty(vehicle.getModel())) {
                this.errosRequest.add("Request without vehicle model.");
            }

            if (Strings.isEmpty(vehicle.getManufacturer())) {
                this.errosRequest.add("Request without vehicle manufacturer.");
            }

            if (Strings.isEmpty(vehicle.getColor())) {
                this.errosRequest.add("Request without vehicle color.");
            }

            if (vehicle.getActive() == null) {
                this.errosRequest.add("Request without vehicle status.");
            }

        }

        if (!CollectionUtils.isEmpty(this.errosRequest)) {
            retorno = false;
        }

        return retorno;

    }

}