package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.dto.AnalyticsDTO.*;
import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.repository.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private AdRepository adRepository;

    public OverviewMetrics getOverviewMetrics(String period) {
        List<Advertisement> allAds = adRepository.findAll();

        if (allAds.isEmpty()) {
            return new OverviewMetrics(0L, 0L, 0L, 0.0, 0L, 0L, 0.0);
        }

        Long totalListings = (long) allAds.size();
        Long pendingApprovals = allAds.stream()
                .filter(ad -> ad.getFlag() == 0)
                .count();
        Long approvedListings = allAds.stream()
                .filter(ad -> ad.getFlag() == 1)
                .count();

        Double totalValue = allAds.stream()
                .filter(ad -> ad.getPrice() != null && !ad.getPrice().trim().isEmpty())
                .mapToDouble(ad -> {
                    try {
                        return Double.parseDouble(ad.getPrice().replaceAll("[^0-9.]", ""));
                    } catch (Exception e) {
                        return 0.0;
                    }
                })
                .sum();

        Long vehiclesWithLeasing = allAds.stream()
                .filter(ad -> ad.getlStatus() != null && ad.getlStatus() == 1)
                .count();

        Long vehiclesWithInsurance = allAds.stream()
                .filter(ad -> ad.getiStatus() != null && ad.getiStatus() == 1)
                .count();

        // Calculate growth rate based on period
        Double growthRate = calculateGrowthRate(allAds, period);

        return new OverviewMetrics(
                totalListings,
                pendingApprovals,
                approvedListings,
                totalValue,
                vehiclesWithLeasing,
                vehiclesWithInsurance,
                growthRate
        );
    }

    public List<ManufacturerStats> getManufacturerStats(String period) {
        List<Advertisement> ads = adRepository.findAll();

        if (ads.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<Advertisement>> grouped = ads.stream()
                .filter(ad -> ad.getManufacturer() != null && !ad.getManufacturer().trim().isEmpty())
                .collect(Collectors.groupingBy(Advertisement::getManufacturer));

        if (grouped.isEmpty()) {
            return new ArrayList<>();
        }

        Long total = (long) ads.size();

        return grouped.entrySet().stream()
                .map(entry -> {
                    Long count = (long) entry.getValue().size();
                    Double percentage = (count * 100.0) / total;
                    Double avgPrice = entry.getValue().stream()
                            .filter(ad -> ad.getPrice() != null && !ad.getPrice().trim().isEmpty())
                            .mapToDouble(ad -> {
                                try {
                                    return Double.parseDouble(ad.getPrice().replaceAll("[^0-9.]", ""));
                                } catch (Exception e) {
                                    return 0.0;
                                }
                            })
                            .average()
                            .orElse(0.0);

                    return new ManufacturerStats(entry.getKey(), count, percentage, avgPrice);
                })
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                .collect(Collectors.toList());
    }

    public List<TimeSeriesData> getTimeSeriesData(String period) {
        List<Advertisement> ads = adRepository.findAll();

        if (ads.isEmpty()) {
            return generateDummyTimeSeriesData(period);
        }

        // Group ads by date based on period
        Map<String, List<Advertisement>> grouped = ads.stream()
                .filter(ad -> ad.getDatetime() != null)
                .collect(Collectors.groupingBy(ad -> formatDateByPeriod(ad.getDatetime(), period)));

        List<TimeSeriesData> result = grouped.entrySet().stream()
                .map(entry -> {
                    Long newListings = (long) entry.getValue().size();
                    Long approved = entry.getValue().stream()
                            .filter(ad -> ad.getFlag() == 1)
                            .count();
                    Long pending = entry.getValue().stream()
                            .filter(ad -> ad.getFlag() == 0)
                            .count();

                    return new TimeSeriesData(entry.getKey(), newListings, approved, pending);
                })
                .sorted(Comparator.comparing(TimeSeriesData::getPeriod))
                .collect(Collectors.toList());

        // If we have data but not enough points, fill with dummy data
        if (result.size() < 5) {
            return generateDummyTimeSeriesData(period);
        }

        return result;
    }

    public List<VehicleTypeStats> getVehicleTypeStats(String period) {
        List<Advertisement> ads = adRepository.findAll();

        if (ads.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Long> typeCount = ads.stream()
                .filter(ad -> ad.getV_type() != null && !ad.getV_type().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        Advertisement::getV_type,
                        Collectors.counting()
                ));

        if (typeCount.isEmpty()) {
            return new ArrayList<>();
        }

        Long total = (long) ads.size();

        return typeCount.entrySet().stream()
                .map(entry -> new VehicleTypeStats(
                        entry.getKey(),
                        entry.getValue(),
                        (entry.getValue() * 100.0) / total
                ))
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                .collect(Collectors.toList());
    }

    public StatusMetrics getStatusMetrics(String period) {
        List<Advertisement> ads = adRepository.findAll();

        if (ads.isEmpty()) {
            return new StatusMetrics(0L, 0L, 0L, 0L);
        }

        Long withLeasing = ads.stream()
                .filter(ad -> ad.getlStatus() != null && ad.getlStatus() == 1)
                .count();
        Long withInsurance = ads.stream()
                .filter(ad -> ad.getiStatus() != null && ad.getiStatus() == 1)
                .count();
        Long withBoth = ads.stream()
                .filter(ad -> ad.getlStatus() != null && ad.getiStatus() != null
                        && ad.getlStatus() == 1 && ad.getiStatus() == 1)
                .count();
        Long withNeither = ads.stream()
                .filter(ad -> (ad.getlStatus() == null || ad.getlStatus() == 0)
                        && (ad.getiStatus() == null || ad.getiStatus() == 0))
                .count();

        return new StatusMetrics(withLeasing, withInsurance, withBoth, withNeither);
    }

    // Helper method to format date based on period
    private String formatDateByPeriod(Date date, String period) {
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        switch (period.toLowerCase()) {
            case "weekly":
                return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-'W'ww"));
            case "quarterly":
                int quarter = (localDateTime.getMonthValue() - 1) / 3 + 1;
                return localDateTime.getYear() + "-Q" + quarter;
            case "yearly":
                return String.valueOf(localDateTime.getYear());
            case "monthly":
            default:
                return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
    }

    // Helper method to calculate growth rate
    private Double calculateGrowthRate(List<Advertisement> ads, String period) {
        try {
            Date now = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);

            // Go back one period
            switch (period.toLowerCase()) {
                case "weekly":
                    cal.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
                case "quarterly":
                    cal.add(Calendar.MONTH, -3);
                    break;
                case "yearly":
                    cal.add(Calendar.YEAR, -1);
                    break;
                case "monthly":
                default:
                    cal.add(Calendar.MONTH, -1);
                    break;
            }

            Date previousPeriodStart = cal.getTime();

            long currentCount = ads.stream()
                    .filter(ad -> ad.getDatetime() != null && ad.getDatetime().after(previousPeriodStart))
                    .count();

            cal.add(Calendar.MONTH, -1); // Go back another period
            Date previousPreviousPeriodStart = cal.getTime();

            long previousCount = ads.stream()
                    .filter(ad -> ad.getDatetime() != null
                            && ad.getDatetime().after(previousPreviousPeriodStart)
                            && ad.getDatetime().before(previousPeriodStart))
                    .count();

            if (previousCount == 0) {
                return currentCount > 0 ? 100.0 : 0.0;
            }

            return ((double) (currentCount - previousCount) / previousCount) * 100;
        } catch (Exception e) {
            return 0.0;
        }
    }

    // Generate dummy data if database is empty or has insufficient data
    private List<TimeSeriesData> generateDummyTimeSeriesData(String period) {
        List<TimeSeriesData> dummy = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 6; i >= 0; i--) {
            LocalDateTime date;
            String periodLabel;

            switch (period.toLowerCase()) {
                case "weekly":
                    date = now.minusWeeks(i);
                    periodLabel = date.format(DateTimeFormatter.ofPattern("yyyy-'W'ww"));
                    break;
                case "quarterly":
                    date = now.minusMonths(i * 3);
                    int quarter = (date.getMonthValue() - 1) / 3 + 1;
                    periodLabel = date.getYear() + "-Q" + quarter;
                    break;
                case "yearly":
                    date = now.minusYears(i);
                    periodLabel = String.valueOf(date.getYear());
                    break;
                case "monthly":
                default:
                    date = now.minusMonths(i);
                    periodLabel = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    break;
            }

            // Generate sample data
            dummy.add(new TimeSeriesData(periodLabel, 0L, 0L, 0L));
        }

        return dummy;
    }
}