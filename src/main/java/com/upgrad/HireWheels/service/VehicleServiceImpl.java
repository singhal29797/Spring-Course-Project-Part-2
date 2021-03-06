package com.upgrad.HireWheels.service;

import com.upgrad.HireWheels.dao.*;
import com.upgrad.HireWheels.entities.Booking;
import com.upgrad.HireWheels.entities.Vehicle;
import com.upgrad.HireWheels.responsemodel.VehicleDetailResponse;
import com.upgrad.HireWheels.util.EntityDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    @Qualifier("vehicleCategoryDAO")
    VehicleCategoryDAO vehicleCategoryDAO;

    @Autowired
    @Qualifier("requestStatusDAO")
    RequestStatusDAO requestStatusDAO;

    @Autowired
    @Qualifier("bookingDAO")
    BookingDAO bookingDAO;

    @Autowired
    @Qualifier("userDAO")
    UserDAO userDAO;

    @Autowired
    @Qualifier("vehicleDAO")
    VehicleDAO vehicleDAO;

    @Autowired
    EntityDTOMapper entityDTOMapper;

    /**
     * Returns all the available vehicle in the requested Category for booking with respect to Date, Location and Availability.
     * @param categoryName
     * @param pickUpDate
     * @param dropDate
     * @param locationId
     * @return
     */

    public List<VehicleDetailResponse> getAvailableVehicles(String categoryName, Date pickUpDate, Date dropDate, int locationId) {
        List<Vehicle> returnedVehicleList = new ArrayList<>();
                vehicleCategoryDAO.findByVehicleCategoryName(categoryName).getVehicleSubCategoriesList().forEach(a-> a.getVehicleList().forEach(b-> {
                    if (b.getLocationWithVehicle().getId() == locationId) {
                        returnedVehicleList.add(b);
                    }
            }));


        List<Integer> bookedVehicleIdList = new ArrayList<>();
        returnedVehicleList.forEach(a-> {
            List<Booking> bookedVehicleList = bookingDAO.findByVehicleWithBooking(a);
            bookedVehicleList.forEach(b ->{
                if ((pickUpDate.after(b.getPickUpDate()) && pickUpDate.before(b.getDropOffDate())) || (dropDate.after(b.getPickUpDate()) && dropDate.before(b.getDropOffDate())) || (pickUpDate.before(b.getPickUpDate()) && dropDate.after(b.getDropOffDate())) || pickUpDate.equals(b.getDropOffDate()) || dropDate.equals(b.getPickUpDate()) || pickUpDate.equals(b.getPickUpDate()) || dropDate.equals(b.getDropOffDate())){
                    bookedVehicleIdList.add(b.getVehicleWithBooking().getId());
                }
            });
        });
        List<VehicleDetailResponse> mapVehicle = new ArrayList<>();
        for (Vehicle v : returnedVehicleList) {
                if(!bookedVehicleIdList.contains(v.getId())){
                    VehicleDetailResponse vehicleDetailResponse = entityDTOMapper.convertToVehicleResponse(v);
                    mapVehicle.add(vehicleDetailResponse);
                }
        }
       return mapVehicle;
    }

    public List<VehicleDetailResponse> getAllVehicleByUserId(int userId) {
        List<VehicleDetailResponse> mapVehicle = new ArrayList<>();
        List<Vehicle> returnedVehicleList;
        if (userId != 1){
            returnedVehicleList = userDAO.findById(userId).get().getVehicleList();
        } else {
            returnedVehicleList = vehicleDAO.findAll();
        }
        for (Vehicle v : returnedVehicleList) {
            VehicleDetailResponse vehicleDetailResponse = new VehicleDetailResponse();
            vehicleDetailResponse.setVehicleId(v.getId());
            vehicleDetailResponse.setVehicleModel(v.getVehicleModel());
            vehicleDetailResponse.setVehicleOwnerId(v.getUser().getId());
            vehicleDetailResponse.setVehicleNumber(v.getVehicleNumber());
            vehicleDetailResponse.setVehicleOwnerName(v.getUser().getFirstName());
            vehicleDetailResponse.setColor(v.getColor());
            vehicleDetailResponse.setCostPerHour(v.getVehicleSubCategory().getPricePerHour());
            vehicleDetailResponse.setFuelType(v.getFuelType().getFuelType());
            vehicleDetailResponse.setLocationId(v.getLocationWithVehicle().getId());
            vehicleDetailResponse.setCarImageUrl(v.getCarImageUrl());
            vehicleDetailResponse.setActivityId(v.getAdminRequest().getActivity().getId());
            vehicleDetailResponse.setRequestStatusId(v.getAdminRequest().getRequestStatus().getId());
            vehicleDetailResponse.setVehicleSubCategoryId(v.getVehicleSubCategory().getId());
            mapVehicle.add(vehicleDetailResponse);
        }
        return mapVehicle;
    }


}

