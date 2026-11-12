package com.example.downloader

import android.net.Uri

object AdBlockManager {

    private val AD_DOMAINS = hashSetOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "google-analytics.com",
        "taboola.com",
        "outbrain.com",
        "adnxs.com",
        "popads.net",
        "propellerads.com",
        "monetag.com",
        "adsterra.com",
        "clickadu.com",
        "exoclick.com",
        "adroll.com",
        "criteo.com",
        "scorecardresearch.com",
        "amazon-adsystem.com",
        "clarity.ms",
        "hotjar.com",
        "histats.com",
        "adservice.google.com"
    )

    fun isAdUrl(url: String): Boolean {
        val lower = url.lowercase()
        try {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase() ?: return false
            for (adDomain in AD_DOMAINS) {
                if (host == adDomain || host.endsWith(".$adDomain")) {
                    return true
                }
            }
        } catch (_: Exception) {}

        // Secondary pattern checks
        return lower.contains("/ads/") ||
                lower.contains("/pagead/") ||
                lower.contains("pubads") ||
                lower.contains("banner_ad") ||
                lower.contains("googleads") ||
                lower.contains("popunder")
    }

    val INJECTED_CSS: String = """
        /* 1. HIDE ALL ORIGINAL SITE BRANDING (HEADER, FOOTER, BLOG, BRAND LOGOS) */
        header,
        footer,
        nav,
        .sticky.top-0,
        [class*="h-nav"],
        [class*="section-divider"],
        .container.max-w-\[800px\],
        div:has(> h2:contains("Latest")),
        a[href*="/blog/"],
        a[href*="getindevice.com/blog"],
        [class*="brand-highlight"]:empty,
        /* 2. AD BLOCKING SELECTORS */
        ins.adsbygoogle,
        [id*="google_ads"],
        [id*="ad-"],
        [id*="ad_"],
        [class*="ad-"],
        [class*="ad_"],
        [class*="adsbygoogle"],
        .ad-container,
        [data-ad-client],
        iframe[src*="doubleclick"],
        iframe[src*="googleads"],
        iframe[src*="ad"],
        .banner-ads,
        #sticky-ad,
        .ad-banner,
        .native-ad,
        div[class*="sponsor"],
        div[class*="banner"] {
            display: none !important;
            height: 0 !important;
            width: 0 !important;
            opacity: 0 !important;
            pointer-events: none !important;
            visibility: hidden !important;
        }

        /* 3. SLEEK MODERN AR-VIDS STYLING */
        body {
            background-color: #0b0f19 !important;
            color: #f1f5f9 !important;
            margin: 0 !important;
            padding: 16px 12px 100px 12px !important;
            font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif !important;
        }

        /* Hero Heading & Downloader Form */
        h1, .heading-xl {
            font-size: 1.5rem !important;
            font-weight: 800 !important;
            color: #ffffff !important;
            margin-top: 8px !important;
            margin-bottom: 12px !important;
            text-align: center !important;
        }

        .downloader-form, form {
            background: #111827 !important;
            border: 1px solid #1f2937 !important;
            border-radius: 20px !important;
            padding: 16px !important;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5) !important;
            margin: 12px auto !important;
            max-width: 600px !important;
        }

        .downloader-input, input[type="url"], input[type="text"] {
            background-color: #1f2937 !important;
            color: #ffffff !important;
            border: 1.5px solid #6366f1 !important;
            border-radius: 12px !important;
            padding: 14px 16px !important;
            font-size: 14px !important;
            width: 100% !important;
            box-sizing: border-box !important;
        }

        .downloader-input::placeholder {
            color: #94a3b8 !important;
        }

        button[type="submit"], .downloader-button, .btn-download {
            background: linear-gradient(135deg, #6366f1, #4f46e5) !important;
            color: #ffffff !important;
            font-weight: 700 !important;
            border-radius: 12px !important;
            padding: 12px 24px !important;
            border: none !important;
            box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4) !important;
            cursor: pointer !important;
            transition: all 0.2s ease !important;
        }

        /* Result cards / format selection */
        .result-box, .video-preview, [class*="download-option"], table {
            background-color: #1e293b !important;
            border-radius: 16px !important;
            border: 1px solid #334155 !important;
            color: #f8fafc !important;
            margin-top: 16px !important;
        }
    """.trimIndent()

    val INJECTED_JS: String = """
        (function() {
            // Function to sanitize any text branding
            function sanitizeText(node) {
                if (node.nodeType === Node.TEXT_NODE) {
                    if (node.nodeValue && /getindevice/i.test(node.nodeValue)) {
                        node.nodeValue = node.nodeValue.replace(/getindevice/gi, 'AR-Vids').replace(/Get In Device/gi, 'AR-Vids');
                    }
                } else if (node.nodeType === Node.ELEMENT_NODE) {
                    if (node.placeholder && /getindevice/i.test(node.placeholder)) {
                        node.placeholder = node.placeholder.replace(/getindevice/gi, 'AR-Vids');
                    }
                    if (node.title && /getindevice/i.test(node.title)) {
                        node.title = node.title.replace(/getindevice/gi, 'AR-Vids');
                    }
                    for (let i = 0; i < node.childNodes.length; i++) {
                        sanitizeText(node.childNodes[i]);
                    }
                }
            }

            // Remove headers, footers and ad nodes directly
            function cleanDOM() {
                const elementsToRemove = document.querySelectorAll('header, footer, [class*="h-nav"], [class*="section-divider"], a[href*="/blog/"], ins.adsbygoogle, iframe[src*="ad"]');
                elementsToRemove.forEach(el => {
                    try { el.remove(); } catch(e) {}
                });
                sanitizeText(document.body || document.documentElement);
                document.title = "AR-Vids";
            }

            // Run initial sweep
            cleanDOM();

            // Set up MutationObserver to keep branding clean and remove injected ads dynamically
            const observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    mutation.addedNodes.forEach(function(node) {
                        if (node.nodeType === Node.ELEMENT_NODE) {
                            if (node.matches && node.matches('header, footer, ins.adsbygoogle, [id*="ad-"], [class*="ad-"], iframe[src*="ad"]')) {
                                try { node.remove(); } catch(e) {}
                                return;
                            }
                            sanitizeText(node);
                        }
                    });
                });
            });

            observer.observe(document.documentElement, { childList: true, subtree: true });

            // Scanner to discover available download formats automatically
            function scanDiscoveredDownloads() {
                const candidates = document.querySelectorAll('a.btn-download, a[download], a[href*=".mp4"], a[href*="download"], table a, .result-box a');
                candidates.forEach(function(el) {
                    var href = el.href || el.getAttribute('data-url') || '';
                    if (href && (href.startsWith('http://') || href.startsWith('https://')) && !href.includes('getindevice.com/blog')) {
                        var text = (el.innerText || el.getAttribute('download') || 'Download Video').trim();
                        if (window.AndroidApp && window.AndroidApp.onMediaDiscovered) {
                            window.AndroidApp.onMediaDiscovered(href, text, 'video/mp4');
                        }
                    }
                });
            }

            // Hook download clicks to trigger native Android download interface
            document.addEventListener('click', function(e) {
                const target = e.target.closest('a[download], a[href*=".mp4"], a[href*="download"], button[class*="download"], a.btn-download');
                if (target) {
                    let href = target.href || target.getAttribute('data-url') || '';
                    if (href && (href.startsWith('http://') || href.startsWith('https://'))) {
                        let filename = target.getAttribute('download') || '';
                        if (window.AndroidApp && window.AndroidApp.onDownloadRequested) {
                            window.AndroidApp.onDownloadRequested(href, filename, 'video/mp4');
                        }
                    }
                }
            }, true);

            // Periodic check for dynamic download buttons
            setInterval(scanDiscoveredDownloads, 800);
        })();
    """.trimIndent()

    fun buildPasteScript(url: String): String {
        val safeUrl = url.replace("'", "\\'").replace("\"", "\\\"").replace("\n", "").trim()
        return """
            (function() {
                var input = document.getElementById('all-video-downloader-url-input') || 
                            document.querySelector('input.downloader-input') || 
                            document.querySelector('input[type="url"]') || 
                            document.querySelector('input[type="text"]');
                if (input) {
                    input.value = '$safeUrl';
                    input.focus();
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                    input.dispatchEvent(new Event('change', { bubbles: true }));

                    var form = input.closest('form') || document.querySelector('form.downloader-form');
                    if (form) {
                        var btn = form.querySelector('button[type="submit"]') || form.querySelector('.downloader-button');
                        if (btn) {
                            btn.click();
                        } else {
                            form.submit();
                        }
                    }
                }
            })();
        """.trimIndent()
    }
}
