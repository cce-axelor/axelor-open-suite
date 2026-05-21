import { expect, test } from '@playwright/test';
import { FormView } from '@e2e/shared/pages/FormView';
import { GridView } from '@e2e/shared/pages/GridView';

test('create a new partner (company)', async ({ page }) => {
  await page.goto('#/ds/referential.root.partners');

  // Default view is "Cards" — switch to list
  await page.getByRole('button', { name: 'Liste' }).click();

  const grid = new GridView(page);
  await expect(grid.gridView).toBeVisible();
  await grid.newRecord();

  const form = new FormView(page);
  await expect(form.formView).toBeVisible();

  // Select type via combobox (enum field)
  await form.getField('partnerTypeSelect').getByRole('combobox').click();
  await page.getByRole('option', { name: 'Société' }).click();

  await form.getField('name').getByRole('textbox').fill('Playwright test partner');

  await form.save();

  await expect(form.getField('name').getByRole('textbox')).toHaveValue('Playwright test partner');
});
