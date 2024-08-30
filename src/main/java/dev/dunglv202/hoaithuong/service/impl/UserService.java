package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import dev.dunglv202.hoaithuong.dto.DetailProfileDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedDetailProfileDTO;
import dev.dunglv202.hoaithuong.dto.UserInfoDTO;
import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.GoogleHelper;
import dev.dunglv202.hoaithuong.model.AppUser;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import dev.dunglv202.hoaithuong.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final AuthHelper authHelper;
    private final UserRepository userRepository;
    private final ConfigService configService;
    private final GoogleHelper googleHelper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
        return new AppUser(user);
    }

    public UserInfoDTO getSignedUserInfo() {
        User signedUser = userRepository.findById(authHelper.getSignedUser().getId())
            .orElseThrow();
        return new UserInfoDTO(signedUser);
    }

    public DetailProfileDTO getDetailProfile() {
        User user = userRepository.findById(authHelper.getSignedUser().getId()).orElseThrow();
        Configuration configs = configService.getConfigsByUser(user);
        return new DetailProfileDTO(user, configs);
    }

    public void updateDetailProfile(UpdatedDetailProfileDTO updateDTO) {
        User signedUser = authHelper.getSignedUser();
        Configuration configs = configService.getConfigsByUser(signedUser);

        // check valid sheet id
        String sheetId = updateDTO.getConfigs().getReportSheetId();
        if (sheetId != null && !sheetId.equals(configs.getReportSheetId()) && !isValidSheetId(sheetId)) {
            throw new ClientVisibleException("{report.google_sheet_id.invalid}");
        }

        configs.mergeWith(updateDTO.getConfigs());
        configService.saveConfigs(configs);
    }

    private boolean isValidSheetId(String sheetId) {
        try {
            Sheets sheetService = googleHelper.getSheetService(authHelper.getSignedUser());
            sheetService.spreadsheets().get(sheetId).execute();
            return true;
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) return false;
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
