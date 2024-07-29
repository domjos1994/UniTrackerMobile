/*
 * Copyright (C)  2019-2024 Domjos
 * This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniTrackerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackerlibrary.tools;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.model.BaseDescriptionObject;

/** @noinspection unused*/
public class Validator {
    private StringBuilder result;

    private final Map<View, String> messages;
    private final Map<View, ValidationExecutor> validationExecutors;
    private final Context context;
    private final int icon;

    public Validator(Context context, int icon) {
        this.context = context;
        this.icon = icon;

        this.result = new StringBuilder();
        this.messages = new LinkedHashMap<>();
        this.validationExecutors = new LinkedHashMap<>();
    }

    public void addEmptyValidator(final EditText field) {
        if(field.getHint()!=null && !field.getHint().toString().isEmpty()) {
            if(!field.getHint().toString().trim().endsWith("*")) {
                field.setHint(field.getHint() + " *");
            }
        }

        this.validationExecutors.put(field, () -> {
            if(field.getText() != null) {
                return !field.getText().toString().isEmpty();
            }
            return false;
        });
        this.messages.put(field, String.format(this.context.getString(R.string.message_validation_empty), field.getHint()));
    }

    public void addEmptyValidator(final Spinner field, final String title) {
        this.validationExecutors.put(field, () -> {
            if(field.getSelectedItem() != null) {
                return !field.getSelectedItem().toString().isEmpty();
            }
            return false;
        });
        this.messages.put(field, String.format(this.context.getString(R.string.message_validation_empty), title));
    }

    public void addLengthValidator(final EditText field, final int minLength, final int maxLength) {
        this.validationExecutors.put(field, () -> {
            if(field.getText()!=null) {
                return field.getText().length() <= maxLength && field.getText().length() >= minLength;
            }
            return false;
        });
        this.messages.put(field, String.format(this.context.getString(R.string.message_validator_length), field.getHint(), maxLength, minLength));
    }

    public void addIntegerValidator(final EditText field) {
        this.validationExecutors.put(field, () -> {
            if(field.getText() != null && !field.getText().toString().isEmpty()) {
                try {
                    Integer.parseInt(field.getText().toString());
                    return true;
                } catch (Exception ignored) {}
            }
            return false;
        });
        this.messages.put(field, String.format(context.getString(R.string.message_validation_integer), field.getHint()));
    }

    public void addDoubleValidator(final EditText field) {
        this.validationExecutors.put(field, () -> {
            if(field.getText() != null && !field.getText().toString().isEmpty()) {
                try {
                    Double.parseDouble(field.getText().toString());
                    return true;
                } catch (Exception ignored) {}
            }
            return false;
        });
        this.messages.put(field, String.format(context.getString(R.string.message_validation_double), field.getHint()));
    }

    public void addDateValidator(final EditText field) {
        this.validationExecutors.put(field, () -> {
            if(field.getText() != null && !field.getText().toString().isEmpty()) {
                try {
                    ConvertHelper.convertStringToDate(field.getText().toString(), this.context);
                    return true;
                } catch (Exception ignored) {}
            }
            return false;
        });
        this.messages.put(field, String.format(context.getString(R.string.message_validation_date), field.getHint()));
    }

    public void addDateValidator(final EditText field, boolean mandatory) {
        this.validationExecutors.put(field, () -> {
            if(field.getText() != null && !field.getText().toString().isEmpty()) {
                try {
                    ConvertHelper.convertStringToDate(field.getText().toString(), this.context);
                    return true;
                } catch (Exception ignored) {
                    return false;
                }
            }
            return !mandatory;
        });
        this.messages.put(field, String.format(context.getString(R.string.message_validation_date), field.getHint()));
    }

    public void addDateValidator(final EditText field, String format) {
        this.validationExecutors.put(field, () -> {
            if(field.getText() != null && !field.getText().toString().isEmpty()) {
                try {
                    ConvertHelper.convertStringToDate(field.getText().toString(), format);
                    return true;
                } catch (Exception ignored) {}
            }
            return false;
        });
        this.messages.put(field, String.format(context.getString(R.string.message_validation_date), field.getHint()));
    }

    public void addDateValidator(final EditText field, String format, boolean mandatory) {
        this.validationExecutors.put(field, () -> {
            if(field.getText() != null && !field.getText().toString().isEmpty()) {
                try {
                    ConvertHelper.convertStringToDate(field.getText().toString(), format);
                    return true;
                } catch (Exception ignored) {
                    return false;
                }
            }
            return !mandatory;
        });
        this.messages.put(field, String.format(context.getString(R.string.message_validation_date), field.getHint()));
    }

    public void addDateValidator(final EditText field, final Date minDate, final Date maxDate) {
        String strMinDate = ConvertHelper.convertDateToString(minDate, this.context);
        String strMaxDate = ConvertHelper.convertDateToString(maxDate, this.context);

        this.validationExecutors.put(field, () -> {
            if(field.getText() != null && !field.getText().toString().isEmpty()) {
                try {
                    Date dt = ConvertHelper.convertStringToDate(field.getText().toString(), this.context);
                    if(dt != null) {
                        if(minDate!=null && maxDate!=null) {
                            if(dt.after(minDate) && dt.before(maxDate)) {
                                return true;
                            }
                        } else if(minDate!=null) {
                            if(dt.after(minDate)) {
                                return true;
                            }
                        } else if(maxDate!=null) {
                            if(dt.before(maxDate)) {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                } catch (Exception ignored) {}
            }
            return true;
        });
        this.messages.put(field, String.format(context.getString(R.string.message_validation_date_min_max), field.getHint(), strMinDate, strMaxDate));
    }

    public void addDateValidator(final EditText field, final Date minDate, final Date maxDate, boolean mandatory) {
        String strMinDate = ConvertHelper.convertDateToString(minDate, this.context);
        String strMaxDate = ConvertHelper.convertDateToString(maxDate, this.context);

        this.validationExecutors.put(field, () -> {
            if(field.getText() != null && !field.getText().toString().isEmpty()) {
                try {
                    Date dt = ConvertHelper.convertStringToDate(field.getText().toString(), this.context);
                    if(dt != null) {
                        if(minDate!=null && maxDate!=null) {
                            if(dt.after(minDate) && dt.before(maxDate)) {
                                return true;
                            }
                        } else if(minDate!=null) {
                            if(dt.after(minDate)) {
                                return true;
                            }
                        } else if(maxDate!=null) {
                            if(dt.before(maxDate)) {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                } catch (Exception ignored) {
                    return false;
                }
            }
            return !mandatory;
        });
        this.messages.put(field, String.format(context.getString(R.string.message_validation_date_min_max), field.getHint(), strMinDate, strMaxDate));
    }

    public void addDateValidator(final EditText field, final Date minDate, final Date maxDate, String format) {
        String strMinDate = ConvertHelper.convertDateToString(minDate, format);
        String strMaxDate = ConvertHelper.convertDateToString(maxDate, format);

        this.validationExecutors.put(field, () -> {
            if(field.getText() != null && !field.getText().toString().isEmpty()) {
                try {
                    Date dt = ConvertHelper.convertStringToDate(field.getText().toString(), format);
                    if(dt!=null) {
                        if(minDate!=null && maxDate!=null) {
                            if(dt.after(minDate) && dt.before(maxDate)) {
                                return true;
                            }
                        } else if(minDate!=null) {
                            if(dt.after(minDate)) {
                                return true;
                            }
                        } else if(maxDate!=null) {
                            if(dt.before(maxDate)) {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                } catch (Exception ignored) {}
            }
            return true;
        });
        this.messages.put(field, String.format(context.getString(R.string.message_validation_date_min_max), field.getHint(), strMinDate, strMaxDate));
    }

    public void addDateValidator(final EditText field, final Date minDate, final Date maxDate, String format, boolean mandatory) {
        String strMinDate = ConvertHelper.convertDateToString(minDate, format);
        String strMaxDate = ConvertHelper.convertDateToString(maxDate, format);

        this.validationExecutors.put(field, () -> {
            if(field.getText() != null && !field.getText().toString().isEmpty()) {
                try {
                    Date dt = ConvertHelper.convertStringToDate(field.getText().toString(), format);
                    if(dt!=null) {
                        if(minDate!=null && maxDate!=null) {
                            if(dt.after(minDate) && dt.before(maxDate)) {
                                return true;
                            }
                        } else if(minDate!=null) {
                            if(dt.after(minDate)) {
                                return true;
                            }
                        } else if(maxDate!=null) {
                            if(dt.before(maxDate)) {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                } catch (Exception ignored) {
                    return false;
                }
            }
            return !mandatory;
        });
        this.messages.put(field, String.format(context.getString(R.string.message_validation_date_min_max), field.getHint(), strMinDate, strMaxDate));
    }

    public void addRegexValidator(EditText field, String regex) {
        this.validationExecutors.put(field, () -> {
            if (field != null) {
                if (field.getText() != null) {
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(field.getText().toString());
                    return matcher.matches();
                }
            }
            return true;
        });
        this.messages.put(field, String.format(this.context.getString(R.string.message_validator_matches), field.getHint()));
    }

    public boolean checkDuplicatedEntry(String value, long id, List<BaseDescriptionObject> items) {
        boolean isOk = true;
        String itemToSave = value.trim().toLowerCase();
        for(BaseDescriptionObject baseDescriptionObject : items) {
            if(id != baseDescriptionObject.getId() || id == 0) {
                String item = baseDescriptionObject.getTitle().trim().toLowerCase();

                if(itemToSave.equals(item)) {
                    isOk = false;
                }
            }
        }

        if(!isOk) {
            this.result.append(String.format(this.context.getString(R.string.message_validator_duplicated), value));
        }
        return isOk;
    }

    public void removeValidator(View view) {
        if(view instanceof EditText) {
            ((EditText) view).setError(null);
        }

        for(Map.Entry<View, ValidationExecutor> executorEntry : this.validationExecutors.entrySet()) {
            if(executorEntry.getKey().getId()==view.getId()) {
                this.validationExecutors.remove(executorEntry.getKey());
                return;
            }
        }
    }

    public boolean getState() {
        this.result = new StringBuilder();
        boolean state = true;
        for(Map.Entry<View, ValidationExecutor> executorEntry : this.validationExecutors.entrySet()) {
            if(!executorEntry.getValue().validate()) {
                if(executorEntry.getKey() instanceof EditText) {
                    ((EditText) executorEntry.getKey()).setError(this.messages.get(executorEntry.getKey()));
                } else {
                    Notifications.printMessage((Activity) this.context, this.messages.get(executorEntry.getKey()), this.icon);
                }
                this.result.append(this.messages.get(executorEntry.getKey())).append("\n");
                state = false;
            } else {
                if(executorEntry.getKey() instanceof EditText) {
                    ((EditText) executorEntry.getKey()).setError(null);
                }
            }
        }
        if(state) {
            this.clear();
        }

        return state;
    }

    public String getResult() {
        return this.result.toString();
    }

    public void clear() {
        this.result = new StringBuilder();
        for(View view : this.validationExecutors.keySet()) {
            if(view instanceof EditText) {
                ((EditText) view).setError(null);
            }
        }
    }

    @FunctionalInterface
    public interface ValidationExecutor {
        boolean validate();
    }
}
