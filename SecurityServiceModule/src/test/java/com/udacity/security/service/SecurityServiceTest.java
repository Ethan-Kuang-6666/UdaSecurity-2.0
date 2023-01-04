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


    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    @DisplayName("Test 1")
    public void alarmArmed_sensorsActivated_pendingAlarm(ArmingStatus armingStatus) {
        Mockito.when(sensor1.getActive()).thenReturn(false);
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor1, true);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    @DisplayName("Test 2")
    public void alarmArmed_sensorActivated_pendingAlarm_changeToAlarm(ArmingStatus armingStatus) {
        Mockito.when(sensor1.getActive()).thenReturn(false);
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor1, true);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }


    @Test
    @DisplayName("Test 3")
    public void pendingAlarm_sensorsInactive_returnToNoAlarm() {
        Mockito.when(sensor1.getActive()).thenReturn(true);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor1, false);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    @DisplayName("Test 4")
    public void activeAlarm_changingSensorsStates_AlarmStateNotChanged() {
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        Mockito.when(sensor1.getActive()).thenReturn(true);
        Mockito.when(sensor2.getActive()).thenReturn(true);
        securityService.changeSensorActivationStatus(sensor1, false);
        Mockito.verify(securityRepository, Mockito.never()).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        Mockito.verify(securityRepository, Mockito.never()).setAlarmStatus(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor2, false);
        Mockito.verify(securityRepository, Mockito.never()).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        Mockito.verify(securityRepository, Mockito.never()).setAlarmStatus(AlarmStatus.NO_ALARM);
    }


    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    @DisplayName("Test 5")
    public void oneSensorActive_anotherBeingActivated_alreadyPending_changeToAlarm(ArmingStatus armingStatus) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        Mockito.when(sensor1.getActive()).thenReturn(false);
        Mockito.when(sensor2.getActive()).thenReturn(false);
        securityService.changeSensorActivationStatus(sensor1, true);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor2, true);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    @DisplayName("Test 6")
    public void sensorDeactivated_alreadyInactive_noChangesToAlarmState() {
        Mockito.when(sensor1.getActive()).thenReturn(false);
        securityService.changeSensorActivationStatus(sensor1, false);
        Mockito.verify(securityRepository, Mockito.never()).setAlarmStatus(AlarmStatus.ALARM);
        Mockito.verify(securityRepository, Mockito.never()).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        Mockito.verify(securityRepository, Mockito.never()).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    @DisplayName("Test 7")
    public void containsCat_armedHome_changeToAlarmStatus() {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Mockito.when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
        securityService.processImage(bufferedImage);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @RepeatedTest(2)
    @DisplayName("Test 8")
    public void doesNotContainsCat_sensorsNotActive_changeToNoAlarm(RepetitionInfo repetitionInfo) {
        Mockito.when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(false);
        Mockito.when(sensor1.getActive()).thenReturn(false);
        Mockito.when(sensor2.getActive()).thenReturn(false);
        if (repetitionInfo.getCurrentRepetition() == 1) {
            Mockito.when(sensor3.getActive()).thenReturn(true);
            sensorSet = new HashSet<>(Arrays.asList(sensor1, sensor2, sensor3));
            Mockito.when(securityService.getSensors()).thenReturn(sensorSet);
            securityService.processImage(bufferedImage);
            Mockito.verify(securityRepository, Mockito.never()).setAlarmStatus(AlarmStatus.ALARM);
            Mockito.verify(securityRepository, Mockito.never()).setAlarmStatus(AlarmStatus.NO_ALARM);
            Mockito.verify(securityRepository, Mockito.never()).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        } else {
            Mockito.when(sensor3.getActive()).thenReturn(false);
            sensorSet = new HashSet<>(Arrays.asList(sensor1, sensor2, sensor3));
            Mockito.when(securityService.getSensors()).thenReturn(sensorSet);
            securityService.processImage(bufferedImage);
            Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
        }
    }

    @Test
    @DisplayName("Test 9")
    public void systemDisarmed_changeToNoAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    @DisplayName("Test 10")
    public void systemArmed_resetAllSensorsToInactive(ArmingStatus armingStatus) {
        sensorSet = new HashSet<>(Arrays.asList(sensor1, sensor2, sensor3));
        Mockito.when(securityService.getSensors()).thenReturn(sensorSet);
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.setArmingStatus(armingStatus);
        Mockito.verify(sensor1).setActive(false);
        Mockito.verify(sensor2).setActive(false);
        Mockito.verify(sensor3).setActive(false);
    }

    @Test
    @DisplayName("Test 11")
    public void armedHomeWithCat_changeToAlarm() {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Mockito.when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
        securityService.processImage(bufferedImage);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        Mockito.verify(securityRepository, Mockito.times(2)).setAlarmStatus(AlarmStatus.ALARM);
    }
}

