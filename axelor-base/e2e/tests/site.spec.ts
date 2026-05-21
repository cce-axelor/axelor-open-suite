import { expect, test } from '@playwright/test';
import { FormView } from '@e2e/shared/pages/FormView';
import { GridView } from '@e2e/shared/pages/GridView';

test('create a new site', async ({ page }) => {
  await page.goto('#/ds/referential.sites');

  const tab = page.getByTestId('tab-panel:referential.sites');
  await expect(tab).toBeVisible();

  const grid = new GridView(page);
  await expect(grid.gridView).toBeVisible();
  await grid.newRecord();

  const form = new FormView(page);
  await expect(form.formView).toBeVisible();

  const code = `TEST-${Date.now()}`;
  await form.getField('code').getByRole('textbox').fill(code);
  await form.getField('name').getByRole('textbox').fill('Playwright test site');

  await form.save();

  await expect(form.getField('code').getByRole('textbox')).toHaveValue(code);
});
