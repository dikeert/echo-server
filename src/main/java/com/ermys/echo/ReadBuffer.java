package com.ermys.echo;

import com.google.inject.BindingAnnotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Target;

@BindingAnnotation
@Target({TYPE, PARAMETER, FIELD, METHOD})
public @interface ReadBuffer {
}
