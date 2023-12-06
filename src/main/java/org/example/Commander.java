package org.example;

import java.util.ArrayList;
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.example.grpc.DroneServiceGrpc;
import com.example.grpc.DroneServiceOuterClass.*;
import org.jetbrains.annotations.NotNull;

public class Commander {
    private ManagedChannel channel;
    private DroneServiceGrpc.DroneServiceBlockingStub stub;
    private Heartbeat heartbeat;
    private DetailedData detailedData;
    public Commander(String ip, int port){
        //TODO: отработать случай, при котором не получается установить соединение с дроном
        channel = ManagedChannelBuilder.forTarget(ip + ":" + port).usePlaintext().build();
        stub = DroneServiceGrpc.newBlockingStub(channel);
        UpdateHeartbeat();
        UpdateDetailedData();
    }
    public Commander(){
    }
    public void ArmDisarm(int arm){
        ArmDisarm request = ArmDisarm.newBuilder().setArm(arm).build();
        LongAnswer response = stub.sendArmDisarm(request);
        System.out.println("ArmDisarm: " + response.getResult());
    }
    public void ChangeFlightMode(AutopilotModes mode){
        ChangeFlightMode request = ChangeFlightMode.newBuilder().setMode(mode).build();
        LongAnswer response = stub.sendChangeFlightMode(request);
        System.out.println("ChangeFlightMode: " + response.getResult());
    }
    public void NavTakeoff(float pitch, float yaw, float latitude, float longitude, float altitude){
        NavTakeoff request = NavTakeoff.newBuilder().setPitch(pitch).setYaw(yaw).setLatitude(latitude).setLongitude(longitude).setAltitude(altitude).build();
        LongAnswer response = stub.sendNavTakeoff(request);
        System.out.println("NavTakeoff: " + response.getResult());
    }
    public void ReturnToLaunch(){
        ReturnToLaunch request = ReturnToLaunch.newBuilder().build();
        LongAnswer response = stub.sendReturnToLaunch(request);
        System.out.println("ReturnToLaunch: " + response.getResult());
    }
    public void StartMission(){
        StartMission request = StartMission.newBuilder().build();
        LongAnswer response = stub.sendStartMission(request);
        System.out.println("StartMission: " + response.getResult());
    }
    public void ClearAllMissions(){
        ClearAllMissions request = ClearAllMissions.newBuilder().build();
        MissionAnswer response = stub.sendClearAllMissions(request);
        System.out.println("ClearAllMissions: " + response.getResult());
    }
    public void SendFlightPlan(List<Geocode> geocodes, int holdTime, int acceptRadius){
        FlightPlan request = FlightPlan.newBuilder().addAllFlightPlan(geocodes).setHoldTime(holdTime).setAcceptRadius(acceptRadius).build();
        MissionAnswer response = stub.sendFlightPlan(request);
        System.out.println("SendFlightPLan: " + response.getResult());
    }
    public void UpdateHeartbeat(){
        HeartbeatRequest request = HeartbeatRequest.newBuilder().build();
        heartbeat = stub.getHeartbeat(request);
    }
    public void UpdateDetailedData(){
        DetailedDataRequest request = DetailedDataRequest.newBuilder().build();
        detailedData = stub.getDetailedData(request);
    }
    public Heartbeat GetHeartbeat(){
        return heartbeat;
    }
    public DetailedData GetDetailedData(){
        return detailedData;
    }
    public List<Geocode> GetGeocodesFromArray(@NotNull double[][] coords){
        for (double[] cord : coords) {
            if(cord.length != 3){
                System.out.println("Incorrect input!");
                return null;
            }
        }

        List<Geocode> geocodes = new ArrayList<>();
        for (double[] cord : coords) {
            Geocode geocode = Geocode.newBuilder().setLatitude((float) cord[0]).setLongitude((float) cord[1]).setAltitude((float) cord[2]).build();
            geocodes.add(geocode);
        }
        return geocodes;
    }
    public void PrintHeartbeat(){
        UpdateHeartbeat();
        StringBuilder sb = new StringBuilder();
        sb.append("Type: ")
                .append(heartbeat.getType())
                .append("; Autopilot: ")
                .append(heartbeat.getAutopilot())
                .append("; Base Mode: ")
                .append(heartbeat.getBaseMode())
                .append("; Custom Mode: ")
                .append(heartbeat.getCustomMode())
                .append("; System Status: ")
                .append(heartbeat.getSystemStatus())
                .append("; Mavlink Version: ")
                .append(heartbeat.getMavlinkVersion());
        System.out.println(sb);
    }
    public void PrintDetailedData(){
        UpdateDetailedData();
        StringBuilder sb = new StringBuilder();
        sb.append("Battery Voltage: ")
                .append(detailedData.getVoltageBattery())
                .append("; Battery Remaining: ")
                .append(detailedData.getBatteryRemaining())
                .append("; Latitude: ")
                .append(detailedData.getLatitude())
                .append("; Longitude: ")
                .append(detailedData.getLongitude())
                .append("; Absolute Altitude: ")
                .append(detailedData.getAbsoluteAltitude())
                .append("; Relative Altitude: ")
                .append(detailedData.getRelativeAltitude())
                .append("; Heading: ")
                .append(detailedData.getHeading())
                .append("; Roll: ")
                .append(detailedData.getRoll())
                .append("; Pitch: ")
                .append(detailedData.getPitch())
                .append("; Yaw: ")
                .append(detailedData.getYaw())
                .append("; Roll Speed: ")
                .append(detailedData.getRollSpeed())
                .append("; Pitch Speed: ")
                .append(detailedData.getPitchSpeed())
                .append("; Yaw Speed: ")
                .append(detailedData.getYawSpeed())
                .append("; Current Mission: ")
                .append(detailedData.getCurrentMission())
                .append("; Mission Count: ")
                .append(detailedData.getMissionCount())
                .append("; Vx: ")
                .append(detailedData.getVx())
                .append("; Vy: ")
                .append(detailedData.getVy())
                .append("; Vz: ")
                .append(detailedData.getVz());
        System.out.println(sb);
    }
    public double[][] GenerateGeoCodes(double initialLat, double initialLong, double alt, double latOffset, double longOffset, int numPoints) {
        double[][] geocodes = new double[numPoints][3];
        int rowCount = (int) Math.sqrt(numPoints);
        int colCount = numPoints / rowCount;

        double currentLat = initialLat;
        double currentLong = initialLong;
        boolean switcher = true;
        int test = 1;
        for(int i = 0; i < numPoints; i++){
            geocodes[i][0] = currentLat;
            geocodes[i][1] = currentLong;
            geocodes[i][2] = alt;

            if(switcher)
                currentLat += latOffset * test;
            else{
                currentLong += longOffset;
                test = test * -1;
            }
            switcher = !switcher;
        }
        return geocodes;
    }
}