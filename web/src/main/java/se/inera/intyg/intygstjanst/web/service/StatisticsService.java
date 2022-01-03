/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.web.service;

public interface StatisticsService {

    /**
     * Sends data to Statistik (ST) about a created certificate (intyg), returning true if successfully sent, false
     * otherwise.
     */
    boolean created(String certificateXml, String certificateId, String certificateType, String careUnitId);

    /**
     * Sends data to Statistic (ST) about a revoked certificate (intyg), returning true if successfully sent,
     * false otherwise.
     */
    boolean revoked(String certificateXml, String certificateId, String certificateType, String careUnitId);

    /**
     * Sends data to Statistic (ST) about a revoked certificate (intyg), returning true if successfully sent,
     * false otherwise.
     */
    boolean sent(String certificateId, String certificateType, String careUnitId, String recipientId);

    /**
     * Sends data to Statistik (ST) about a sent message, returning true if data was successfully sent, false otherwise.
     */
    boolean messageSent(String xml, String messageId, String topic);
}
