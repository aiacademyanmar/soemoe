(() => {
  'use strict';
  const SITE_URL = 'https://aaii.asia/dama/psychology/';
  const API_URL = '../api/index.php';
  const tg = window.Telegram && window.Telegram.WebApp ? window.Telegram.WebApp : null;
  let sessionToken = sessionStorage.getItem('bp_tg_session') || '';
  const $ = (selector) => document.querySelector(selector);

  function toast(message) {
    const el = $('#toast');
    if (!el) return;
    el.textContent = message;
    el.classList.add('show');
    window.setTimeout(() => el.classList.remove('show'), 2600);
  }
  function showScreen(name) {
    document.querySelectorAll('.screen').forEach((el) => el.classList.toggle('active', el.id === 'screen-' + name));
    document.querySelectorAll('.nav-item').forEach((el) => el.classList.toggle('active', el.dataset.tab === name));
    if (tg && tg.HapticFeedback && typeof tg.HapticFeedback.selectionChanged === 'function') tg.HapticFeedback.selectionChanged();
  }
  function openWebsite() {
    if (tg && typeof tg.openLink === 'function') tg.openLink(SITE_URL);
    else window.open(SITE_URL, '_blank', 'noopener');
  }
  async function api(action, options = {}) {
    const headers = Object.assign({'Accept':'application/json'}, options.headers || {});
    if (options.body) headers['Content-Type'] = 'application/json';
    if (sessionToken) headers.Authorization = 'Bearer ' + sessionToken;
    const response = await fetch(API_URL + '?action=' + encodeURIComponent(action), Object.assign({}, options, {headers}));
    const payload = await response.json().catch(() => ({}));
    if (!response.ok || payload.ok === false) throw new Error(payload.message || payload.error || 'Request failed');
    return payload;
  }
  function renderChapters(chapters) {
    const list = $('#chapter-list');
    if (!list) return;
    list.replaceChildren();
    chapters.forEach((chapter) => {
      const row = document.createElement('div'); row.className = 'chapter-row';
      const number = document.createElement('div'); number.className = 'chapter-num';
      number.textContent = String(chapter.number).padStart(2, '0');
      const copy = document.createElement('div'); copy.className = 'chapter-copy';
      const title = document.createElement('b'); title.textContent = chapter.title;
      const label = document.createElement('small');
      label.textContent = chapter.access === 'free' ? 'FREE · အခမဲ့' : 'PREMIUM · Premium လိုအပ်သည်';
      copy.append(title, label);
      const action = document.createElement('button');
      action.className = 'chapter-action ' + (chapter.access === 'free' ? 'free' : '');
      action.textContent = chapter.access === 'free' ? 'ဖွင့်ရန်' : '🔒';
      action.addEventListener('click', () => {
        if (chapter.access === 'free') openWebsite();
        else { showScreen('membership'); toast('Premium ခွင့်ပြုချက်ကို မူရင်း Admin Server မှ စစ်ဆေးရမည်။'); }
      });
      row.append(number, copy, action); list.append(row);
    });
  }
  function setAccount(user) {
    $('#account-name').textContent = user.first_name || 'Telegram User';
    $('#account-handle').textContent = user.username ? '@' + user.username : 'Telegram ID: ' + user.telegram_user_id;
    $('#avatar').textContent = (user.first_name || 'BP').trim().slice(0, 1).toUpperCase();
    $('#login-status').textContent = 'ချိတ်ဆက်ပြီး';
    $('#link-status').textContent = 'Server API စစ်ဆေးရန်လို';
    $('#premium-status').textContent = 'မူရင်း Server မှ စစ်ဆေးရန်လို';
  }
  async function startTelegramLogin() {
    if (!tg || !tg.initData) {
      $('#account-name').textContent = 'Telegram မှ ဖွင့်ပါ';
      $('#account-handle').textContent = 'Browser Preview Mode';
      $('#login-status').textContent = 'Telegram Login မရရှိပါ';
      return;
    }
    try {
      const result = await api('telegram_login', {method:'POST', body:JSON.stringify({init_data:tg.initData})});
      sessionToken = result.session_token;
      sessionStorage.setItem('bp_tg_session', sessionToken);
      setAccount({first_name:result.user.first_name,username:result.user.username,telegram_user_id:result.user.telegram_user_id});
    } catch (error) {
      $('#login-status').textContent = 'Login မအောင်မြင်ပါ';
      toast('Telegram Login ကို Server မှ အတည်မပြုနိုင်သေးပါ။');
    }
  }
  async function loadCatalog() {
    try {
      const catalog = await api('public_catalog');
      renderChapters(catalog.chapters || []);
    } catch (error) {
      renderChapters([
        {number:1,title:'Buddha Psychology and Its Future',access:'free'},
        {number:2,title:'Neuroplasticity and Vipassana',access:'premium'},
        {number:3,title:'The Roots of Mental Distress',access:'premium'},
        {number:4,title:'The Four Noble Truths and Psychological Interpretation',access:'premium'},
        {number:5,title:'The Five Aggregates and the Mind',access:'premium'},
        {number:6,title:'Non-Self and Mental Suffering',access:'premium'},
        {number:7,title:'Dependent Origination and Emotions',access:'premium'},
        {number:8,title:'Mindfulness and Therapeutic Perspectives',access:'premium'},
        {number:9,title:'Practical Benefits of Buddhist Psychology',access:'premium'}
      ]);
      toast('စာရင်းကို Offline Preview ဖြင့် ပြသနေသည်။');
    }
  }
  document.querySelectorAll('[data-go]').forEach((el) => el.addEventListener('click', () => showScreen(el.dataset.go)));
  document.querySelectorAll('[data-tab]').forEach((el) => el.addEventListener('click', () => showScreen(el.dataset.tab)));
  $('#website-button')?.addEventListener('click', openWebsite);
  $('#refresh-button')?.addEventListener('click', () => { loadCatalog(); toast('ပြန်လည်စစ်ဆေးနေသည်…'); });
  if (tg) {
    tg.ready(); tg.expand();
    if (tg.setHeaderColor) tg.setHeaderColor('#11152f');
    if (tg.setBackgroundColor) tg.setBackgroundColor('#101329');
  }
  loadCatalog(); startTelegramLogin();
})();