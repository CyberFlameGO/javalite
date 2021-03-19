package org.javalite.activejdbc.validation;

import org.javalite.validation.AttributePresenceValidator;
import org.junit.Test;

import static org.javalite.test.jspec.JSpec.the;

/**
 * Testing non-model capabilities.
 */
public class StandaloneValidationSpec {


    @Test
    public void shouldValidatePresence(){
        Book book = new Book();
        book.setTitle("12 Chairs");
        the(book).shouldNotBe("valid");

        the(book.validators().size()).shouldBeEqual(2);
        the(book.validators().get(0)).shouldBeA(AttributePresenceValidator.class);
        the(book.validators().get(1)).shouldBeA(AttributePresenceValidator.class);

        book.setAuthorFirstName("Ilya");
        the(book).shouldBe("valid");
    }

    @Test
    public void shouldValidatePresenceForChildModel(){

        FictionBook fb = new FictionBook();
        fb.setTitle("Hitchhikers Guide to the Galaxy");

        the(fb).shouldNotBe("valid");

        fb.setAuthorFirstName("Douglas Adams");
        the(fb).shouldBe("valid");
    }
}
