package main.java.utility;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.ParseException;

public class CurrencyFormatter extends TextFormatter<Double> {
    private static final double DEFAULT_VALUE = 0.00;
    private static final String CURRENCY_SYMBOL = "$";

    private static final DecimalFormat strictZeroDecimalFormat  
        = new DecimalFormat(CURRENCY_SYMBOL + "###,##0.00");

    public CurrencyFormatter() {
        super(
                // string converter converts between a string and a value property.
                new StringConverter<Double>() {
                    @Override
                    public String toString(Double value) {
                        return strictZeroDecimalFormat.format(value);
                    }

                    @Override
                    public Double fromString(String string) {
                        try {
                            return strictZeroDecimalFormat.parse(string).doubleValue();
                        } catch (ParseException e) {
                            return Double.NaN;
                        }
                    }
                },
                DEFAULT_VALUE,
                // change filter rejects text input if it cannot be parsed.
                change -> {
                    try {
                        strictZeroDecimalFormat.parse(change.getControlNewText());
                        return change;
                    } catch (ParseException e) {
                        return null;
                    }
                }
        );
    }
}
