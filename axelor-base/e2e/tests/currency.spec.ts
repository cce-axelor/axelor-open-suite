import { expect, test } from '@playwright/test';
import { FormView } from '@e2e/shared/pages/FormView';
import { GridView } from '@e2e/shared/pages/GridView';

test('create a new currency', async ({ page }) => {
  await page.goto('#/ds/admin.root.general.currency');

  const grid = new GridView(page);
  await expect(grid.gridView).toBeVisible();
  await grid.newRecord();

  const form = new FormView(page);
  await expect(form.formView).toBeVisible();

  const code = `TST-${Date.now()}`;
  await form.getField('code').getByRole('textbox').fill(code);
  await form.getField('name').getByRole('textbox').fill('Playwright test currency');
  await form.getField('symbol').getByRole('textbox').fill('T');
  await form.getField('codeISO').getByRole('textbox').fill(code);
  await form.getField('numberOfDecimals').getByRole('spinbutton').fill('3');

  await form.save();

  await expect(form.getField('code').getByRole('textbox')).toHaveValue(code);
});

test('edit an existing currency', async ({ page }) => {
  await page.goto('#/ds/admin.root.general.currency');

  const grid = new GridView(page);
  await expect(grid.gridView).toBeVisible();
  await grid.newRecord();

  const form = new FormView(page);
  const code = `TST-${Date.now()}`;
  await form.getField('code').getByRole('textbox').fill(code);
  await form.getField('name').getByRole('textbox').fill('Currency to edit');
  await form.getField('symbol').getByRole('textbox').fill('T');
  await form.getField('codeISO').getByRole('textbox').fill(code);
  await form.getField('numberOfDecimals').getByRole('spinbutton').fill('3');
  await form.save();

  // Go back to the list from the current form
  await page.getByRole('button', { name: 'Liste' }).click();
  await expect(grid.gridView).toBeVisible();

  // Filter by code to find the record regardless of pagination
  await grid.gridView.getByTestId('advance-search-input').getByRole('textbox').fill(code);
  await grid.gridView.getByTestId('advance-search-input').getByRole('textbox').press('Enter');

  // Double-click to open in form view (single click opens inline grid editing)
  await grid.gridView.getByRole('row').filter({ hasText: code }).dblclick();

  // Switch to edit mode (records opened from the list are read-only)
  await expect(form.formView).toBeVisible();
  await form.edit();
  await form.getField('name').getByRole('textbox').fill('Edited currency');
  await form.save();

  await expect(form.getField('name').getByRole('textbox')).toHaveValue('Edited currency');
});

test('delete a currency', async ({ page }) => {
  await page.goto('#/ds/admin.root.general.currency');

  const grid = new GridView(page);
  await expect(grid.gridView).toBeVisible();
  await grid.newRecord();

  const form = new FormView(page);
  const code = `TST-${Date.now()}`;
  await form.getField('code').getByRole('textbox').fill(code);
  await form.getField('name').getByRole('textbox').fill('Currency to delete');
  await form.getField('symbol').getByRole('textbox').fill('T');
  await form.getField('codeISO').getByRole('textbox').fill(code);
  await form.save();

  // Delete via the "more" menu
  await form.delete();

  // Confirm in the dialog
  const dialog = page.getByTestId(/^dialog:\d+$/).last();
  await expect(dialog).toBeVisible();
  await dialog.getByTestId('btn-confirm').click();

  // After deletion, redirected back to the list
  await expect(grid.gridView).toBeVisible();
});
