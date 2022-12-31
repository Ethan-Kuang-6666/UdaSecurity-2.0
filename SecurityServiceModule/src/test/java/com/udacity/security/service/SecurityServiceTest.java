package com.udacity.security.service;

import com.udacity.image.service.ImageService;
import com.udacity.security.data.AlarmStatus;
import com.udacity.security.data.ArmingStatus;
import com.udacity.security.data.SecurityRepository;
import com.udacity.security.data.Sensor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
    Set<Sensor> sensorSet = new HashSet<>(Arrays.asList(sensor1, sensor2, sensor3));


    @RepeatedTest(2)
    public void alarm_armed_and_sensors_activated_test(RepetitionInfo repetitionInfo) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY, ArmingStatus.ARMED_HOME);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        Mockito.when(sensor1.getActive()).thenReturn(false);
        securityService = new SecurityService(securityRepository, imageService);
        securityService.changeSensorActivationStatus(sensor1, true);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @RepeatedTest(2)
    public void alarm_armed_and_sensor_activated_pending_alarm_test() {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY, ArmingStatus.ARMED_HOME);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Mockito.when(sensor1.getActive()).thenReturn(false);
        securityService = new SecurityService(securityRepository, imageService);
        securityService.changeSensorActivationStatus(sensor1, true);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void pending_alarm_inactive_sensors_test() {
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Mockito.when(sensor1.getActive()).thenReturn(true);
        securityService = new SecurityService(securityRepository, imageService);
        securityService.changeSensorActivationStatus(sensor1, false);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @RepeatedTest(2)
    public void active_alarm_changing_sensors_test() {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY, ArmingStatus.ARMED_HOME);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

    }
}

