package net.p455w0rd.wirelesscraftingterminal.api.recipes;

import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.api.exceptions.MissingIngredientError;
import net.p455w0rd.wirelesscraftingterminal.api.exceptions.RegistrationError;

public interface IIngredient {

    /**
     * Acquire a single input stack for the current recipe, if more then one ItemStack is possible a RegistrationError
     * exception will be thrown, ignore these and let the system handle the error.
     *
     * @return a single ItemStack for the recipe handler.
     *
     * @throws RegistrationError
     * @throws MissingIngredientError
     */
    ItemStack getItemStack() throws RegistrationError, MissingIngredientError;

    /**
     * Acquire a list of all the input stacks for the current recipe, this is for handlers that support multiple inputs
     * per slot.
     *
     * @return an array of ItemStacks for the recipe handler.
     *
     * @throws RegistrationError
     * @throws MissingIngredientError
     */
    ItemStack[] getItemStackSet() throws RegistrationError, MissingIngredientError;

    /**
     * If you wish to support air, you must test before getting the ItemStack, or ItemStackSet
     *
     * @return true if this slot contains no ItemStack, this is passed as "_"
     */
    boolean isAir();

    /**
     * @return The Name Space of the item. Prefer getItemStack or getItemStackSet
     */
    String getNameSpace();

    /**
     * @return The Name of the item. Prefer getItemStack or getItemStackSet
     */
    String getItemName();

    /**
     * @return The Damage Value of the item. Prefer getItemStack or getItemStackSet
     */
    int getDamageValue();

    /**
     * @return The Damage Value of the item. Prefer getItemStack or getItemStackSet
     */
    int getQty();

    /**
     * Bakes the lists in for faster runtime look-ups.
     *
     * @throws MissingIngredientError
     * @throws RegistrationError
     */
    void bake() throws RegistrationError, MissingIngredientError;
}
