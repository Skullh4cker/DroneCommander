package org.example;

import com.example.grpc.DroneServiceOuterClass.AutopilotModes;

public class Main {
    public static void main(String[] args) {
        var commander = new Commander("localhost", 1604);

        double[][] geocodes = commander.GenerateGeoCodes(-35.3636810, 149.1641343, 20,
                0.0005, -0.0005, 1);

        commander.SendFlightPlan(commander.GetGeocodesFromArray(geocodes), 5, 3);

        commander.ChangeFlightMode(AutopilotModes.GUIDED);

        commander.ArmDisarm(1);
        commander.StartMission();
        //commander.ClearAllMissions();
    }
}