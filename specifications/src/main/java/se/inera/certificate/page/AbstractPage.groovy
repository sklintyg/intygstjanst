package se.inera.certificate.page

import geb.Page

abstract class AbstractPage extends Page {

    def doneLoading() {
        js.doneLoading && js.dialogDoneLoading
    }
}
