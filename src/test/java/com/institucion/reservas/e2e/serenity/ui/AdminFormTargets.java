package com.institucion.reservas.e2e.serenity.ui;

import net.serenitybdd.screenplay.playwright.Target;

/**
 * Campos de los formularios de administración de usuarios y recursos, y
 * acciones del detalle (ver, editar, inactivar, cambiar estado).
 */
public final class AdminFormTargets {

    public static final Target USER_NAME =
            Target.the("campo de nombre completo").locatedBy("#user-name");
    public static final Target USER_DOCUMENT =
            Target.the("campo de documento").locatedBy("#user-document");
    public static final Target USER_EMAIL =
            Target.the("campo de correo electrónico").locatedBy("#user-email");
    public static final Target REGISTER_USER_BUTTON =
            Target.the("botón de registrar usuario").locatedBy("button:has-text('Registrar usuario')");

    public static final Target RESOURCE_NAME =
            Target.the("campo de nombre del recurso").locatedBy("#resource-name");
    public static final Target RESOURCE_DESCRIPTION =
            Target.the("campo de descripción").locatedBy("#resource-description");
    public static final Target RESOURCE_TYPE =
            Target.the("campo de tipo").locatedBy("#resource-type");
    public static final Target RESOURCE_CAPACITY =
            Target.the("campo de capacidad").locatedBy("#resource-capacity");
    public static final Target REGISTER_RESOURCE_BUTTON =
            Target.the("botón de registrar recurso").locatedBy("button:has-text('Registrar recurso')");

    public static final Target EDIT_RESOURCE_NAME =
            Target.the("campo de edición de nombre del recurso")
                    .locatedBy("section:has(h2:has-text('Editar recurso')) #resource-name");
    public static final Target SAVE_RESOURCE_BUTTON =
            Target.the("botón de guardar cambios").locatedBy("button:has-text('Guardar cambios')");

    public static final Target DEACTIVATE_USER_BUTTON =
            Target.the("botón de inactivar usuario").locatedBy("button:has-text('Inactivar usuario')");
    public static final Target EDIT_RESOURCE_BUTTON =
            Target.the("botón de editar recurso").locatedBy("button:has-text('Editar recurso')");
    public static final Target STATUS_SELECT =
            Target.the("selector de cambio de estado").locatedBy("#resource-quick-status");
    public static final Target UPDATE_STATUS_BUTTON =
            Target.the("botón de actualizar estado").locatedBy("button:has-text('Actualizar estado')");

    public static Target viewDetailButton(String entityName) {
        return Target.the("botón de ver detalle de " + entityName)
                .locatedBy("tr:has-text('" + entityName + "') button:has-text('Ver detalle')");
    }

    public static Target entityRow(String entityName) {
        return Target.the("fila de " + entityName)
                .locatedBy("tr:has-text('" + entityName + "')");
    }

    private AdminFormTargets() {
    }
}
