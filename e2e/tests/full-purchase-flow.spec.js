import { test, expect } from '@playwright/test'
import crypto from 'node:crypto'

const PAYMENT_SERVICE_URL = process.env.PAYMENT_SERVICE_URL || 'http://localhost:8084'
const STRIPE_WEBHOOK_SECRET = process.env.STRIPE_WEBHOOK_SECRET || 'whsec_e2e_test_secret'

function signStripePayload(payload, secret) {
  const timestamp = Math.floor(Date.now() / 1000)
  const signedPayload = `${timestamp}.${payload}`
  const signature = crypto.createHmac('sha256', secret).update(signedPayload, 'utf8').digest('hex')
  return `t=${timestamp},v1=${signature}`
}

function buildPaymentSucceededEvent(paymentIntentId, amountCents) {
  return JSON.stringify({
    id: `evt_e2e_${Date.now()}`,
    object: 'event',
    api_version: '2023-10-16',
    type: 'payment_intent.succeeded',
    data: {
      object: {
        id: paymentIntentId,
        object: 'payment_intent',
        amount: amountCents,
        currency: 'eur',
        status: 'succeeded',
      },
    },
  })
}

test('registracija -> prijava -> pretraga -> korpa -> porudžbina -> plaćanje -> SSE notifikacija', async ({
  page,
  request,
}) => {
  const stamp = Date.now()
  const email = `e2e.${stamp}@chronoshop.rs`
  const password = 'E2ePassword123!'

  await test.step('Registracija', async () => {
    await page.goto('/register')
    await page.locator('.field', { hasText: /^Ime$/ }).locator('input').fill('E2E')
    await page.locator('.field', { hasText: /^Prezime$/ }).locator('input').fill('Test')
    await page.locator('input[type="email"]').fill(email)
    await page.locator('input[type="password"]').first().fill(password)
    await page.locator('input[type="password"]').last().fill(password)
    await page.getByRole('button', { name: 'Registruj se' }).click()
    await expect(page).toHaveURL('/')
    await expect(page.locator('.nav-user-desktop')).toHaveText('E2E Test')
  })

  await test.step('Odjava pa prijava (posebno testira /auth/login)', async () => {
    await page.getByRole('button', { name: 'Odjava' }).click()
    await page.goto('/login')
    await page.locator('input[type="email"]').fill(email)
    await page.locator('input[type="password"]').fill(password)
    await page.getByRole('button', { name: 'Prijavi se' }).click()
    await expect(page.locator('.nav-user-desktop')).toHaveText('E2E Test')
  })

  await test.step('Pretraga kataloga i dodavanje u korpu', async () => {
    await page.goto('/catalog')
    await page.getByPlaceholder('Pretraži po nazivu, referenci ili brendu…').fill('Submariner')
    const watchCard = page.locator('.watch-card', { hasText: 'Submariner Date' })
    await expect(watchCard).toBeVisible()
    await watchCard.getByRole('button', { name: 'U korpu' }).click()
    await expect(page.locator('.cart-link .badge')).toHaveText('1')
  })

  let orderNumber
  let paymentIntentId
  let amount

  await test.step('Kreiranje porudžbine', async () => {
    await page.goto('/cart')
    await page.getByRole('button', { name: 'Nastavi na plaćanje' }).click()
    await expect(page).toHaveURL('/checkout')

    await page.locator('.field', { hasText: 'Ulica i broj' }).locator('input').fill('Kneza Miloša 10')
    await page.locator('.field', { hasText: 'Grad' }).locator('input').fill('Beograd')
    await page.locator('.field', { hasText: 'Poštanski broj' }).locator('input').fill('11000')
    await page.locator('.field', { hasText: 'Država' }).locator('input').fill('Srbija')

    const [createIntentResponse] = await Promise.all([
      page.waitForResponse((r) => r.url().includes('/api/payments/create-intent')),
      page.getByRole('button', { name: 'Potvrdi i nastavi na plaćanje' }).click(),
    ])

    test.skip(
      !createIntentResponse.ok(),
      'STRIPE_SECRET_KEY nije podešen kao pravi Stripe test-mode ključ (i dalje sk_test_xxx placeholder) - preskačem deo sa plaćanjem i SSE notifikacijom dok se ne doda pravi GitHub secret.'
    )

    const body = await createIntentResponse.json()
    orderNumber = body.orderNumber
    paymentIntentId = body.paymentIntentId
    amount = body.amount
    expect(orderNumber).toBeTruthy()
    expect(paymentIntentId).toBeTruthy()
  })

  await test.step('Simulacija Stripe webhook-a (payment_intent.succeeded)', async () => {
    const amountCents = Math.round(Number(amount) * 100)
    const payload = buildPaymentSucceededEvent(paymentIntentId, amountCents)
    const signature = signStripePayload(payload, STRIPE_WEBHOOK_SECRET)

    const webhookResponse = await request.post(`${PAYMENT_SERVICE_URL}/api/payments/webhook`, {
      data: payload,
      headers: {
        'Content-Type': 'application/json',
        'Stripe-Signature': signature,
      },
    })
    expect(webhookResponse.ok()).toBeTruthy()
  })

  await test.step('SSE notifikacija stiže uživo dok je EventSource već otvoren', async () => {
    const toast = page.locator('.toast-payment_completed')
    await expect(toast).toBeVisible({ timeout: 10_000 })
    await expect(toast).toContainText(orderNumber)
  })

  await test.step('Trajna potvrda: status porudžbine je PAID', async () => {
    await page.goto('/orders')
    const orderCard = page.locator('.card', { hasText: orderNumber })
    await expect(orderCard.locator('.pill-PAID')).toBeVisible()
  })
})
