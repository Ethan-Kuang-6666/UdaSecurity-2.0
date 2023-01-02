package com.udacity.security.service;

import com.udacity.image.service.ImageService;
import com.udacity.security.data.AlarmStatus;
import com.udacity.security.data.ArmingStatus;
import com.udacity.security.data.SecurityRepository;
import com.udacity.security.data.Sensor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService;
    @Mock
    private ImageService imageService;
    @Mock
    private SecurityRepository securityRepository;
    @Mock
    private Sensor sensor1;
    @Mock
    private Sensor sensor2;
    @Mock
    private Sensor sensor3;
    Set<Sensor> sensorSet;
    @Mock
    BufferedImage bufferedImage;


    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }


    @Test
    public void alarm_armed_and_sensors_activated_test() {
        Mockito.when(sensor1.getActive()).thenReturn(false);
        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        securityService.changeSensorActivationStatus(sensor1, true);
        Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
    }

    @Test
    public void alarm_armed_and_sensor_activated_pending_alarm_test() {
        Mockito.when(sensor1.getActive()).thenReturn(false);
        Mockito.when(sensor2.getActive()).thenReturn(false);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService.changeSensorActivationStatus(sensor1, true);
        Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        securityService.changeSensorActivationStatus(sensor2, true);
        Assertions.assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }

    // Not sure if test3 is fine.
    @RepeatedTest(2)
    public void pending_alarm_inactive_sensors_test(RepetitionInfo repetitionInfo) {
        Mockito.when(sensor1.getActive()).thenReturn(true);
        securityService.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        if (repetitionInfo.getCurrentRepetition() == 1) {
            securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        } else {
            securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        }
        securityService.changeSensorActivationStatus(sensor1, false);
        Assertions.assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus());
    }

    @RepeatedTest(2)
    public void active_alarm_changing_sensors_test(RepetitionInfo repetitionInfo) {
        if (repetitionInfo.getCurrentRepetition() == 1) {
            securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        } else {
            securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        }
        Mockito.when(sensor1.getActive()).thenReturn(false);
        Mockito.when(sensor2.getActive()).thenReturn(false);
        securityService.changeSensorActivationStatus(sensor1, true);
        Mockito.when(sensor1.getActive()).thenReturn(true);
        Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
        securityService.changeSensorActivationStatus(sensor2, true);
        Mockito.when(sensor2.getActive()).thenReturn(true);
        Assertions.assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
        securityService.changeSensorActivationStatus(sensor1, false);
        Assertions.assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
        securityService.changeSensorActivationStatus(sensor2, false);
        Assertions.assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }


    @RepeatedTest(2)
    public void one_sensor_active_another_being_activated_test(RepetitionInfo repetitionInfo) {
        if (repetitionInfo.getCurrentRepetition() == 1) {
            securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        } else {
            securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        }
        Mockito.when(sensor1.getActive()).thenReturn(false);
        Mockito.when(sensor2.getActive()).thenReturn(false);
        securityService.changeSensorActivationStatus(sensor1, true);
        Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
        securityService.changeSensorActivationStatus(sensor2, true);
        Assertions.assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }

    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    public void sensor_deactivated_already_inactive_test(AlarmStatus alarmStatus) {
        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        securityService.setAlarmStatus(alarmStatus);
        Mockito.when(sensor1.getActive()).thenReturn(false);
        securityService.changeSensorActivationStatus(sensor1, false);
        Assertions.assertEquals(alarmStatus, securityService.getAlarmStatus());
    }

    @Test
    public void contains_cat_and_armed_home_test() {
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        Mockito.when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
        securityService.processImage(bufferedImage);
        Assertions.assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }

    @RepeatedTest(2)
    public void does_not_contains_cat_test(RepetitionInfo repetitionInfo) {
        Mockito.when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(false);
        Mockito.when(sensor1.getActive()).thenReturn(false);
        Mockito.when(sensor2.getActive()).thenReturn(false);
        securityService.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        if (repetitionInfo.getCurrentRepetition() == 1) {
            Mockito.when(sensor3.getActive()).thenReturn(true);
            sensorSet = new HashSet<>(Arrays.asList(sensor1, sensor2, sensor3));
            Mockito.when(securityService.getSensors()).thenReturn(sensorSet);
            securityService.processImage(bufferedImage);
            Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
        } else {
            Mockito.when(sensor3.getActive()).thenReturn(false);
            sensorSet = new HashSet<>(Arrays.asList(sensor1, sensor2, sensor3));
            Mockito.when(securityService.getSensors()).thenReturn(sensorSet);
            securityService.processImage(bufferedImage);
            Assertions.assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus());
        }
    }

    @RepeatedTest(2)
    public void system_disarmed_test(RepetitionInfo repetitionInfo) {
        securityService.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        if (repetitionInfo.getCurrentRepetition() == 1) {
            securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        } else {
            securityService.setAlarmStatus(AlarmStatus.ALARM);
            securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        }
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        Assertions.assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus());
    }

    @RepeatedTest(2)
    public void system_armed_test(RepetitionInfo repetitionInfo) {
        sensorSet = new HashSet<>(Arrays.asList(sensor1, sensor2, sensor3));
        Mockito.when(securityService.getSensors()).thenReturn(sensorSet);
        if (repetitionInfo.getCurrentRepetition() == 1) {
            securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        } else {
            securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        }
        Mockito.verify(sensor1).setActive(false);
        Mockito.verify(sensor2).setActive(false);
        Mockito.verify(sensor3).setActive(false);
    }

    @RepeatedTest(2)
    public void armed_home_with_cat(RepetitionInfo repetitionInfo) {
        if (repetitionInfo.getCurrentRepetition() == 1) {
            securityService.setArmingStatus(ArmingStatus.DISARMED);
        } else {
            securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        }
        Mockito.when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
        securityService.processImage(bufferedImage);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        Assertions.assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }
}

