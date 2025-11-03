package com.sandhyasofttech.gstbillingpro.invoice;

public class GstCalculationUtil {

    public static class GstDetails {
        public double cgst;
        public double sgst;
        public double igst;

        public GstDetails(double cgst, double sgst, double igst) {
            this.cgst = cgst;
            this.sgst = sgst;
            this.igst = igst;
        }
    }

    /**
     * Calculate CGST & SGST for intra-state, or IGST for inter-state
     * @param taxableValue Value before tax
     * @param gstPercent GST percentage, eg 18 for 18%
     * @param isIntraState true if intra-state
     * @return GstDetails with calculated tax components
     */
    public static GstDetails calculateGst(double taxableValue, double gstPercent, boolean isIntraState) {
        if (isIntraState) {
            double halfTax = taxableValue * gstPercent / 2 / 100.0;
            return new GstDetails(halfTax, halfTax, 0);
        } else {
            double igstTax = taxableValue * gstPercent / 100.0;
            return new GstDetails(0, 0, igstTax);
        }
    }
}
