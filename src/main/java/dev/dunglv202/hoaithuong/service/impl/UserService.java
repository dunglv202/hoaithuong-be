package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.DetailProfileDTO;
import dev.dunglv202.hoaithuong.dto.UpdateAvatarRespDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedDetailProfileDTO;
import dev.dunglv202.hoaithuong.dto.UserInfoDTO;
import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.FileUtil;
import dev.dunglv202.hoaithuong.model.SheetInfo;
import dev.dunglv202.hoaithuong.model.auth.AppUser;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import dev.dunglv202.hoaithuong.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    private final AuthHelper authHelper;
    private final UserRepository userRepository;
    private final ConfigService configService;
    private final StorageService storageService;
    private final SpreadsheetService spreadsheetService;
    private final CalendarService calendarService;
    private final ScheduleService scheduleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
        return new AppUser(user);
    }

    public UserInfoDTO getSignedUserInfo() {
        User signedUser = userRepository.findById(authHelper.getSignedUserRef().getId())
            .orElseThrow();
        return new UserInfoDTO(signedUser);
    }

    public DetailProfileDTO getDetailProfile() {
        User user = userRepository.findById(authHelper.getSignedUserRef().getId()).orElseThrow();
        Configuration configs = configService.getConfigsByUser(user);
        return new DetailProfileDTO(user, configs);
    }

    public void updateDetailProfile(UpdatedDetailProfileDTO updateDTO) {
        User signedUser = userRepository.findById(authHelper.getSignedUserRef().getId()).orElseThrow();
        Configuration configs = configService.getConfigsByUser(signedUser);

        // check valid sheet
        SheetInfo generalReport = new SheetInfo(
            updateDTO.getConfigs().getGeneralReportId(),
            updateDTO.getConfigs().getGeneralReportSheet()
        );
        if (!generalReport.equals(configs.getGeneralSheetInfo()) && !spreadsheetService.isValidSheet(signedUser, generalReport)) {
            throw new ClientVisibleException("{report.general.spreadsheet.invalid}");
        }
        SheetInfo detailReport = new SheetInfo(
            updateDTO.getConfigs().getDetailReportId(),
            updateDTO.getConfigs().getDetailReportSheet()
        );
        if (!detailReport.equals(configs.getDetailSheetInfo()) && !spreadsheetService.isValidSheet(signedUser, detailReport)) {
            throw new ClientVisibleException("{report.detail.spreadsheet.invalid}");
        }

        // update calendar if needed
        String newCalendarId = updateDTO.getConfigs().getCalendarId();
        if (configs.getCalendarId() == null || !configs.getCalendarId().equals(newCalendarId)) {
            if (!calendarService.isValidCalendar(signedUser, newCalendarId)) {
                throw new ClientVisibleException("{user.calendar.invalid}");
            }
            scheduleService.syncToNewCalendarAsync(signedUser);
        }

        signedUser.setDisplayName(updateDTO.getDisplayName());
        userRepository.save(signedUser);
        configService.saveConfigs(configs.mergeWith(updateDTO.getConfigs()));
    }

    @Transactional
    public UpdateAvatarRespDTO updateAvatar(MultipartFile file) {
        if (file.isEmpty()) throw new ClientVisibleException("{file.invalid}");
        if (!FileUtil.isSupportedImage(file)) throw new ClientVisibleException("{file.format.unsupported}");

        // update avatar & delete old file
        User signedUser = userRepository.findById(authHelper.getSignedUserRef().getId()).orElseThrow();
        String url = storageService.storeFile(file);
        String old = signedUser.getAvatar();
        signedUser.setAvatar(url);
        userRepository.save(signedUser);
        storageService.deleteFile(old);

        return new UpdateAvatarRespDTO(url);
    }
}
