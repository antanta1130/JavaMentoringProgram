package com.mail.dto.input;

import lombok.Getter;
import lombok.Setter;

public class MailFiltrationDto {
    @Getter
    @Setter
    private String dateFrom;

    @Getter
    @Setter
    private String dateTo;

    @Getter
    @Setter
    private String searchInput;

}
