package com.vitaxa.jasteambot;

import ch.qos.logback.classic.Level;
import com.vitaxa.jasteambot.handler.SimpleUserHandler;
import com.vitaxa.jasteambot.handler.UserHandler;
import com.vitaxa.jasteambot.helper.CommonHelper;
import com.vitaxa.jasteambot.helper.ConcurrentHelper;
import com.vitaxa.jasteambot.helper.IOHelper;
import com.vitaxa.jasteambot.helper.SecurityHelper;
import com.vitaxa.jasteambot.service.log.LogManager;
import com.vitaxa.jasteambot.steam.event.Event;
import com.vitaxa.jasteambot.steam.trade.Trade;
import com.vitaxa.jasteambot.steam.trade.TradeManager;
import com.vitaxa.jasteambot.steam.trade.offer.TradeAssetsState;
import com.vitaxa.jasteambot.steam.trade.offer.TradeOffer;
import com.vitaxa.jasteambot.steam.trade.offer.TradeOfferManager;
import com.vitaxa.jasteambot.steam.web.SteamCommunity;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import com.vitaxa.jasteambot.steam.web.SteamWebApi;
import com.vitaxa.jasteambot.steam.web.http.HttpMethod;
import com.vitaxa.jasteambot.steam.web.http.HttpParameters;
import com.vitaxa.steamauth.AuthenticatorLinker;
import com.vitaxa.steamauth.SteamGuardAccount;
import com.vitaxa.steamauth.TimeAligner;
import com.vitaxa.steamauth.UserLogin;
import com.vitaxa.steamauth.exception.WGTokenInvalidException;
import com.vitaxa.steamauth.helper.Json;
import com.vitaxa.steamauth.model.Confirmation;
import com.vitaxa.steamauth.model.FinalizeResult;
import com.vitaxa.steamauth.model.LinkResult;
import com.vitaxa.steamauth.model.LoginResult;
import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import uk.co.thomasc.steamkit.base.generated.enums.*;
import uk.co.thomasc.steamkit.steam3.handlers.steamapps.callbacks.AccountLimitCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamapps.callbacks.VACStatusCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.SteamFriends;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callback.FriendAddedCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callback.FriendMsgCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callback.FriendsListCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.types.Friend;
import uk.co.thomasc.steamkit.steam3.handlers.steamgamecoordinator.SteamGameCoordinator;
import uk.co.thomasc.steamkit.steam3.handlers.steamnotifications.Notification;
import uk.co.thomasc.steamkit.steam3.handlers.steamnotifications.callback.ItemAnnouncementsCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamnotifications.callback.UserNotificationsCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.SteamTrading;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.callbacks.SessionStartCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.callbacks.TradeProposedCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.callbacks.TradeResultCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamunifiedmessages.SteamUnifiedMessages;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.SteamUser;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.*;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.MachineAuthDetails;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.OTPDetails;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackManager;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.ICallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.DisconnectedCallback;
import uk.co.thomasc.steamkit.types.JobID;
import uk.co.thomasc.steamkit.types.SteamID;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Bot {

    protected final Logger log;
    protected final BotConfig botConfig;
    protected final SteamFriends steamFriends;
    protected final SteamClient steamClient;
    protected final SteamTrading steamTrade;
    protected final SteamGameCoordinator steamGC;
    protected final SteamUser steamUser;
    protected final SteamUnifiedMessages steamUnifiedMessages;
    protected final SteamWeb steamWeb;
    protected final SteamCommunity steamCommunity;
    protected final SteamWebApi steamWebApi;
    private final Thread botThread;
    private final Map<SteamID, UserHandler> userHandlers;
    private final List<Friend> friendList = Collections.synchronizedList(new ArrayList<>());
    private final Event<Void> onCloseEvent = new Event<>();
    private CallbackManager callbackManager;
    private Thread tradeOfferThread;
    private Trade currentTrade; // The current trade the bot is in.
    private TradeManager tradeManager;
    private TradeOfferManager tradeOfferManager;
    private volatile boolean isTradeOfferActive; // Is trade offer thread running
    private volatile boolean isRunning; // Is bot thread running
    private volatile boolean isLoggedIn; // Is bot fully Logged in.
    private boolean cookiesAreInvalid = true;
    private String myUserNonce;
    private String myUniqueId;
    private LogOnDetails logOnDetails;
    private SteamGuardAccount steamGuardAccount;
    private CountDownLatch loginLatch;

    public Bot(BotConfig botConfig) {
        final boolean debug = Boolean.getBoolean("jasteam.debug");
        this.log = LogManager.getInstance().createNewLogger(botConfig.getUsername(), botConfig.getLogFile(),
                debug ? Level.DEBUG : Level.INFO);

        this.botConfig = Objects.requireNonNull(botConfig, "botConfig config can't be empty");

        this.botThread = CommonHelper.newThread(String.format("Bot Thread-%s", botConfig.getUsername()), true, this::doWork);

        this.userHandlers = new WeakHashMap<>();
        this.steamWeb = new SteamWeb();
        steamWeb.setResponseCallback(httpResponse -> {
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 400 || statusCode == 403) {
                log.warn("Bad response. Trying to re-authenticate");
                requestWepApiAuth();
            }
        });

        log.debug("Initializing Steam Bot...");
        this.steamClient = new SteamClient();
        this.callbackManager = new CallbackManager(steamClient);
        this.steamTrade = steamClient.getHandler(SteamTrading.class);
        this.steamUser = steamClient.getHandler(SteamUser.class);
        this.steamFriends = steamClient.getHandler(SteamFriends.class);
        this.steamGC = steamClient.getHandler(SteamGameCoordinator.class);
        this.steamUnifiedMessages = steamClient.getHandler(SteamUnifiedMessages.class);
        this.steamCommunity = new SteamCommunity(this);
        this.steamWebApi = new SteamWebApi(this);

        final LogOnDetails logOnDetails = new LogOnDetails();
        logOnDetails.setUsername(botConfig.getUsername());
        logOnDetails.setPassword(botConfig.getPassword());
        this.logOnDetails = logOnDetails;
    }

    /**
     * Starts the callback thread and connects to Steam via SteamKit.
     */
    public final void start() {
        isRunning = true;
        log.debug("Connecting to steam...");
        if (!botThread.isAlive()) botThread.start();
        steamClient.connect();
    }

    /**
     * Disconnect from the Steam network and stop the callback
     */
    public final void stop() {
        CommonHelper.newThread("Bot Shutdown Thread", false, () -> {
            log.info("Trying to shutdown bot ({}) thread.", botConfig.getUsername());
            steamClient.disconnect();

            try {
                if (isLoggedIn) {
                    loginLatch.await(5, TimeUnit.SECONDS);
                }

                isRunning = false;

                botThread.interrupt();

                userHandlers.clear();

                log.info("Bot ({}) successfully stopped", botConfig.getUsername());

                LogManager.getInstance().getLogger(botConfig.getUsername()).detachAndStopAllAppenders();
            } catch (InterruptedException e) {
                log.error("Stop bot was interrupted", e);
            } finally {
                onCloseEvent.handleEvent();
            }
        }).start();
    }

    /**
     * Creates a new trade with the given partner.
     *
     * @return true, if trade was opened
     * false if there is another trade that must be closed first
     */
    public final boolean openTrade(SteamID other) {
        if (currentTrade != null || !checkCookies()) {
            return false;
        }
        steamTrade.trade(other);
        return true;
    }

    /**
     * Closes the current active trade.
     */
    public final void closeTrade() {
        if (currentTrade == null) {
            return;
        }

        unsubscribeTrade(getUserHandler(currentTrade.getOtherSID()), currentTrade);
        tradeManager.stopTrade();
        currentTrade = null;
    }

    private void doWork() {
        subscribeCallBackMessage();
        while (isRunning) {
            try {
                callbackManager.runWaitCallbacks(800L);

                if (tradeOfferManager != null) {
                    tradeOfferManager.handleNextPendingTradeOfferUpdate();
                }
            } catch (Exception e) {
                log.error("Unhandled exception occurred in bot", e);
            }
        }
    }

    private void subscribeCallBackMessage() {
        callbackManager.subscribe(ConnectedCallback.class, this::onConnected);
        callbackManager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        callbackManager.subscribe(LoggedOffCallback.class, this::onLoggedOff);
        callbackManager.subscribe(DisconnectedCallback.class, this::onDisconnected);
        callbackManager.subscribe(LoginKeyCallback.class, this::onLoginKey);
        callbackManager.subscribe(AccountInfoCallback.class, this::onAccountInfo);
        callbackManager.subscribe(UpdateMachineAuthCallback.class, this::onUpdateMachineAuth);
        callbackManager.subscribe(FriendMsgCallback.class, this::onFriendMessage);
        callbackManager.subscribe(SessionStartCallback.class, this::onTradeSessionStart);
        callbackManager.subscribe(TradeProposedCallback.class, this::onTradeProposed);
        callbackManager.subscribe(TradeResultCallback.class, this::onTradeResult);
        callbackManager.subscribe(FriendsListCallback.class, this::onFriendList);
        callbackManager.subscribe(FriendAddedCallback.class, this::onFriendAdded);
        callbackManager.subscribe(UserNotificationsCallback.class, this::onUserNotifications);
        callbackManager.subscribe(ItemAnnouncementsCallback.class, this::onItemAnnouncements);
        callbackManager.subscribe(WalletInfoCallback.class, this::onWalletInfo);
        callbackManager.subscribe(VACStatusCallback.class, this::onVACStatus);
        callbackManager.subscribe(AccountLimitCallback.class, this::onAccountLimit);
    }

    protected <T extends ICallbackMsg> void subscribeCallBackMessage(Class<? extends T> callbackType, Consumer<T> callbackFunc) {
        callbackManager.subscribe(callbackType, callbackFunc);
    }

    // ### CALLBACK BLOCK START ###

    protected void onConnected(ConnectedCallback callback) {
        log.info("Connected to Steam! Logging in " + botConfig.getUsername() + "...");
        userLogOn();
    }

    protected void onLoggedOn(LoggedOnCallback callback) {
        loginLatch = new CountDownLatch(1);
        log.debug("Logged On Callback: {}", callback.getResult());

        if (callback.getResult() == EResult.OK) {
            myUserNonce = callback.getWebAPIUserNonce();

            // If we read myUniqueId from loginKey file, then we'll try to logon
            if (myUniqueId != null) {
                userWebLogOn();
                getUserHandler(steamClient.getSteamID()).onLoginCompleted();
            }
            if (steamGuardAccount == null) {
                initSteamGuardAccount();
            }
            log.info("Successfully logged on!");
            return;
        } else {
            log.debug("Login Error: {}", callback.getResult());
        }

        if (callback.getResult() == EResult.AccountLoginDeniedNeedTwoFactor) {
            String mobileAuthCode = getMobileAuthCode();
            if (mobileAuthCode.isEmpty()) {
                log.error("Failed to generate 2FA code. Make sure you have linked the authenticator via SteamBot.");
                stop();
                return;
            } else {
                logOnDetails.setTwoFactorCode(mobileAuthCode);
                log.info("Generated 2FA code.");
            }
        } else if (callback.getResult() == EResult.TwoFactorCodeMismatch) {
            TimeAligner.alignTime();
            logOnDetails.setTwoFactorCode(steamGuardAccount.generateSteamGuardCode());
            log.info("Regenerated 2FA code.");
        } else if (callback.getResult() == EResult.AccountLogonDenied) {
            log.info("This account is SteamGuard enabled. Enter the authentication code.");
            logOnDetails.setAuthCode(readLine());
        } else if (callback.getResult() == EResult.InvalidLoginAuthCode) {
            log.info("The given SteamGuard code was invalid. Try again.");
            logOnDetails.setAuthCode(readLine());
        } else if (callback.getResult() == EResult.InvalidPassword) {
            log.error("The given password was invalid or too many retries");
            stop();
            return;
        } else if (callback.getResult() == EResult.RateLimitExceeded) {
            log.error("Too many retries. Try again later");
            stop();
            return;
        }
        steamClient.disconnect();
        log.info("Reconnecting in 2...");
        ConcurrentHelper.sleep(2);
        steamClient.connect();
    }

    protected void onLoggedOff(LoggedOffCallback callback) {
        log.warn("Logged off Steam. Reason: {}", callback.getResult());
        if (isLoggedIn) {
            loginLatch.countDown();
        }
        isLoggedIn = false;
        closeTrade();
        cancelTradeOfferPollingThread();
        if (callback.getResult() == EResult.ServiceUnavailable) {
            // Try to reconnect
            log.info("Reconnecting...");
            steamClient.connect();
        }
    }

    protected void onDisconnected(DisconnectedCallback callback) {
        log.warn("Disconnected from Steam Network! User initiate: {}", callback.isUserInitiated());
        if (isLoggedIn) {
            loginLatch.countDown();
        }
        isLoggedIn = false;
        if (!callback.isUserInitiated()) {
            // Try to reconnect
            log.info("Reconnecting...");
            steamClient.connect();
        }
    }

    protected void onLoginKey(LoginKeyCallback callback) {
        myUniqueId = String.valueOf(callback.getUniqueID());

        // Complete auth if it hasn't completed yet
        if (!isLoggedIn) {
            userWebLogOn();
            getUserHandler(steamClient.getSteamID()).onLoginCompleted();
        }
        try {
            IOHelper.write(JaSteamServer.KEY_DIR.resolve(String.format("%s.loginkey", logOnDetails.getUsername())),
                    callback.getLoginKey() + "_" + myUniqueId);
            steamUser.acceptNewLoginKey(callback);
        } catch (IOException e) {
            log.error("Write loginKey exception", e);
        }
    }

    protected void onAccountInfo(AccountInfoCallback callback) {
        steamFriends.setPersonaName(botConfig.getDisplayNamePrefix() + " " + botConfig.getDisplayName());
        steamFriends.setPersonaState(EPersonaState.Online);
    }

    protected void onUpdateMachineAuth(UpdateMachineAuthCallback callback) {
        log.info("Received updated sentry file: {}", callback.getFileName());

        Path sentryFile = JaSteamServer.SENTRY_DIR.resolve(String.format("%s.sentryfile", logOnDetails.getUsername()));

        try {
            IOHelper.write(sentryFile, callback.getData());
        } catch (IOException e) {
            log.error("Unable to write sentry file", e);
        }

        OTPDetails otpDetails = new OTPDetails();
        otpDetails.setIdentifier(callback.getOneTimePassword().getIdentifier());
        otpDetails.setType(callback.getOneTimePassword().getType());

        MachineAuthDetails auth = new MachineAuthDetails();
        auth.setJobID(new JobID(callback.getJobID().getValue()));
        auth.setFileName(callback.getFileName());
        auth.setBytesWritten(callback.getBytesToWrite());
        auth.setFileSize(callback.getData().length);
        auth.setOffset(callback.getOffset());
        auth.seteResult(EResult.OK);
        auth.setLastError(0);
        auth.setOneTimePassword(otpDetails);
        auth.setSentryFileHash(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA1, callback.getData()));

        steamUser.sendMachineAuthResponse(auth);
    }

    protected void onFriendMessage(FriendMsgCallback callback) {
        if (!isLoggedIn) return;

        final EChatEntryType type = callback.getEntryType();
        if (type == EChatEntryType.ChatMsg) {
            log.info("Chat message from {}: {}", callback.getSender().convertToUInt64(), callback.getMessage());
            getUserHandler(callback.getSender()).onMessageHandler(callback.getMessage(), type);
        }
    }

    protected void onTradeSessionStart(SessionStartCallback callback) {
        if (!isLoggedIn) return;

        boolean started = handleTradeSessionStart(callback.getOtherClient());
        if (!started) {
            log.error("Could not start the trade session.");
        } else {
            log.debug("SteamTrading.SessionStartCallback handled successfully. Trade Opened.");
        }
    }

    protected void onTradeProposed(TradeProposedCallback callback) {
        if (!isLoggedIn) return;

        if (!checkCookies()) {
            steamTrade.respondToTrade(callback.getTradeID(), false);
            return;
        }

        if (currentTrade == null && getUserHandler(callback.getOtherClient()).onTradeRequest()) {
            steamTrade.respondToTrade(callback.getTradeID(), true);
        } else {
            steamTrade.respondToTrade(callback.getTradeID(), false);
        }
    }

    protected void onTradeResult(TradeResultCallback callback) {
        if (callback.getResponse() == EEconTradeResponse.Accepted) {
            log.debug("Trade Status: {}", callback.getResponse());
            log.info("Trade Accepted! Client: {}", callback.getOtherClient().convertToUInt64());
            getUserHandler(callback.getOtherClient()).onTradeRequestReply(false, callback.getResponse().toString());
        } else {
            log.warn("Trade failed: {}. Client: {}", callback.getResponse(), callback.getOtherClient().convertToUInt64());
            closeTrade();
            getUserHandler(callback.getOtherClient()).onTradeRequestReply(false, callback.getResponse().toString());
        }
    }

    protected void onFriendList(FriendsListCallback callback) {
        for (Friend friend : callback.getFriendList()) {
            if (friend.getRelationship() == EFriendRelationship.Friend) {
                friendList.add(friend);
            }
            if (friend.getRelationship() == EFriendRelationship.RequestRecipient) {
                if (getUserHandler(friend.getSteamID()).onFriendAdd()) {
                    // this user has added us, let's add him back
                    steamFriends.addFriend(friend.getSteamID());
                }
            } else if (friend.getRelationship() == EFriendRelationship.None) {
                friendList.remove(friend);
            }
        }
    }

    protected void onFriendAdded(FriendAddedCallback callback) {
        log.info(callback.getPersonaName() + " is now a friend");
    }

    protected void onUserNotifications(UserNotificationsCallback callback) {
        for (Notification notification : callback.getNotifications()) {
            log.info("Notification: type: {}, count: {}", notification.getType(), notification.getCount());
        }
    }

    protected void onItemAnnouncements(ItemAnnouncementsCallback callback) {
        log.info("{} new items in our inventory", callback.getCount());
    }

    protected void onWalletInfo(WalletInfoCallback callback) {
        if (callback.isHasWallet()) {
            log.info("Our wallet balance is {} {}", callback.getBalance(), callback.getCurrency().name());
        }
    }

    protected void onVACStatus(VACStatusCallback callback) {
        log.info("We have {} VAC ban {}.", callback.getNumBans(), callback.getNumBans() == 1 ? "" : "s");
    }

    protected void onAccountLimit(AccountLimitCallback callback) {
        final List<String> limitations = new ArrayList<>();
        if (callback.isLimited()) {
            limitations.add("LIMITED");
        }
        if (callback.isCommunityBanned()) {
            limitations.add("COMMUNITY BANNED");
        }
        if (callback.isLocked()) {
            limitations.add("LOCKED");
        }
        if (!callback.isCanInviteFriends()) {
            limitations.add("CAN'T INVITE FRIEND");
        }
        if (limitations.isEmpty()) {
            log.info("Our account has no limitations.");
        } else {
            log.warn("Our account is {}.", String.join(", ", limitations));
        }
    }

    // ### CALLBACK BLOCK END ###

    /**
     * Subscribes all listeners of this to the trade.
     */
    private void subscribeTrade(Trade trade, UserHandler handler) {
        trade.getOnAwaitingConfirmationEvent().addEventListener(handler::onTradeAwaitingConfirmation);
        trade.getOnCloseEvent().addEventListener(args -> handler.onTradeClose());
        trade.getOnErrorEvent().addEventListener(handler::onTradeError);
        trade.getOnStatusErrorEvent().addEventListener(handler::onStatusError);
        trade.getOnAfterInitEvent().addEventListener(args -> handler.onTradeInit());
        trade.getOnUserAddItemEvent().addEventListener(handler::onTradeAddItem);
        trade.getOnUserRemoveItemEvent().addEventListener(handler::onTradeRemoveItem);
        trade.getOnMessageEvent().addEventListener(handler::onTradeMessage);
        trade.getOnUserSetReadyEvent().addEventListener(handler::onTradeReadyHandler);
        trade.getOnUserAcceptEvent().addEventListener(args -> handler.onTradeAcceptHandler());
    }

    /**
     * Unsubscribes all listeners of this from the current trade.
     */
    private void unsubscribeTrade(UserHandler handler, Trade trade) {
        trade.getOnAwaitingConfirmationEvent().clear();
        trade.getOnCloseEvent().clear();
        trade.getOnErrorEvent().clear();
        trade.getOnStatusErrorEvent().clear();
        trade.getOnAfterInitEvent().clear();
        trade.getOnUserAddItemEvent().clear();
        trade.getOnUserRemoveItemEvent().clear();
        trade.getOnMessageEvent().clear();
        trade.getOnUserSetReadyEvent().clear();
        trade.getOnUserAcceptEvent().clear();
    }

    private void subscribeTradeOffer(TradeOfferManager tradeOfferManager) {
        tradeOfferManager.getOnTradeOfferUpdated().addEventListener(this::tradeOfferRouter);
    }

    private void unsubscribeTradeOffer(TradeOfferManager tradeOfferManager) {
        tradeOfferManager.getOnTradeOfferUpdated().removeEventListener(this::tradeOfferRouter);
    }

    /**
     * Create a new trade offer with the specified partner
     *
     * @param other            SteamId of the partner
     * @param tradeAssetsState our/their items
     * @return new trade offer
     */
    public final TradeOffer newTradeOffer(SteamID other, TradeAssetsState tradeAssetsState) {
        return tradeOfferManager.newOffer(other, tradeAssetsState);
    }

    public final void acceptAllMobileTradeConfirmations() {
        try {
            for (Confirmation confirmation : fetchConfirmation()) {
                if (steamGuardAccount.acceptConfirmation(confirmation)) {
                    log.info("Trade confirmed (ID: {}).", confirmation.getId());
                } else {
                    log.debug("Trade ID - {} accepting fail. Creator: {}", confirmation.getId(), confirmation.getCreator());
                }
            }
        } catch (WGTokenInvalidException e) {
            log.error("Invalid session when trying to fetch trade confirmations.", e);
        }
    }

    public final void acceptMobileTradeConfirmation(TradeOffer tradeOffer) {
        try {
            Arrays.stream(fetchConfirmation())
                    .filter(confirmation -> confirmation.getCreator().equals(Long.parseLong(tradeOffer.getTradeOfferId())))
                    .findFirst()
                    .ifPresent(confirmation -> steamGuardAccount.acceptConfirmation(confirmation));
            log.info("Offer \"{}\" with \"{}\" successfully confirmed",
                    tradeOffer.getTradeOfferId(), tradeOffer.getPartnerSteamId().convertToUInt64());
        } catch (WGTokenInvalidException e) {
            log.error("Invalid session when trying to fetch trade confirmations.", e);
        }
    }

    private Confirmation[] fetchConfirmation() throws WGTokenInvalidException {
        if (steamGuardAccount == null) {
            log.warn("Bot account does not have 2FA enabled.");
            return new Confirmation[0];
        }
        steamGuardAccount.getSession().setSteamLogin(steamWeb.getToken());
        steamGuardAccount.getSession().setSteamLoginSecure(steamWeb.getTokenSecure());
        return steamGuardAccount.fetchConfirmations();
    }

    public final String getMobileAuthCode() {
        if (steamGuardAccount == null) {
            initSteamGuardAccount();
        }
        final String steamGuardCode = steamGuardAccount.generateSteamGuardCode();
        log.info("Generated SteamGuard code {}", steamGuardCode);
        return steamGuardCode;
    }

    /**
     * Link a mobile authenticator to bot account, using SteamTradeOffersBot as the authenticator.
     * Called from bot manager console. Usage: "exec [index] linkauth"
     * If successful, 2FA will be required upon the next login.
     * Use "exec [index] getauth" if you need to get a Steam Guard code for the account.
     * To deactivate the authenticator, use "exec [index] unlinkauth".
     */
    public final void linkMobileAuth() {
        UserLogin login = new UserLogin(logOnDetails.getUsername(), logOnDetails.getPassword());
        LoginResult loginResult = login.doLogin();
        if (loginResult == LoginResult.NEED_EMAIL) {
            while (loginResult == LoginResult.NEED_EMAIL) {
                log.info("Enter Steam Guard code from email");
                login.setEmailCode(readLine());
                loginResult = login.doLogin();
            }
        }
        if (loginResult == LoginResult.LOGIN_OKAY) {
            log.info("Linking mobile authenticator...");
            AuthenticatorLinker authLinker = new AuthenticatorLinker(login.getSession());
            LinkResult addAuthResult = authLinker.addAuthenticator();
            if (addAuthResult == LinkResult.MUST_PROVIDE_PHONE_NUMBER) {
                while (addAuthResult == LinkResult.MUST_PROVIDE_PHONE_NUMBER) {
                    log.info("Enter phone number with country code, e.g. +7XXXXXXXXXXX");
                    authLinker.setPhoneNumber(readLine());
                    addAuthResult = authLinker.addAuthenticator();
                }
            }
            if (addAuthResult == LinkResult.AWAITING_FINALIZATION) {
                steamGuardAccount = authLinker.getLinkedAccount();
                try {
                    Path authFile = JaSteamServer.AUTH_DIR.resolve(String.format("%s.auth", logOnDetails.getUsername()));
                    IOHelper.write(authFile, IOHelper.encode(Json.getInstance().toJson(steamGuardAccount)));
                    log.info("Enter SMS code");
                    FinalizeResult authResult = authLinker.finalizeAddAuthenticator(readLine());
                    if (authResult == FinalizeResult.SUCCESS) {
                        log.info("Linked authenticator");
                    } else {
                        log.error("Error linking authenticator" + authResult);
                    }
                } catch (IOException e) {
                    log.error("Failed to save auth file. Aborting authentication.", e);
                }
            } else {
                log.error("Error adding authenticator: " + addAuthResult);
            }
        } else {
            if (loginResult == LoginResult.NEED_2FA) {
                log.error("Mobile authenticator has already been linked!");
            } else {
                log.error("Error performing mobile login: " + loginResult);
            }
        }
    }

    private void initSteamGuardAccount() {
        Path authFile = JaSteamServer.AUTH_DIR.resolve(String.format("%s.auth", logOnDetails.getUsername()));
        if (IOHelper.exists(authFile)) {
            try {
                steamGuardAccount = Json.getInstance().fromJson(IOHelper.newReader(authFile), SteamGuardAccount.class);
            } catch (IOException e) {
                log.error("Can't initialize steamGuardAccount", e);
            }
        } else {
            log.warn("Auth file missing. You need to link mobile auth to enable steamGuard");
        }
    }

    private void tradeOfferRouter(TradeOffer offer) {
        getUserHandler(offer.getPartnerSteamId()).onTradeOfferUpdated(offer);
    }

    private void userWebLogOn() {
        do {
            isLoggedIn = steamWeb.authenticate(myUniqueId, steamClient, myUserNonce, botConfig.getApiKey());

            if (!isLoggedIn) {
                log.warn("Authentication failed, retrying in 5s...");
                ConcurrentHelper.sleep(5);
            }
        } while (!isLoggedIn);

        log.info("User Authenticated!");

        log.debug("Send HEAD request to complete auth (create all necessary cookies)");
        steamWeb.fetch("https://steamcommunity.com/", new HttpParameters(HttpMethod.HEAD));

        log.debug("Init trade manager");
        initTradeManager();

        cookiesAreInvalid = false;
    }

    private void userLogOn() {
        // get key login file which has the login key info saved
        final Path loginKeyFile = JaSteamServer.KEY_DIR.resolve(String.format("%s.loginkey", logOnDetails.getUsername()));

        if (IOHelper.exists(loginKeyFile)) {
            try {
                final String value = IOHelper.decode(IOHelper.read(loginKeyFile));
                final String[] split = value.split("_");
                logOnDetails.setLoginKey(split[0]);
                myUniqueId = split[1];
            } catch (IOException e) {
                log.error("Can't read login key from file: {}", loginKeyFile.getFileName());
                log.error("Read loginKey file exception", e);
            }
        }

        // get sentry file which has the machine hw info saved
        // from when a steam guard code was entered
        final Path sentryFile = JaSteamServer.SENTRY_DIR.resolve(String.format("%s.sentryfile", logOnDetails.getUsername()));

        if (IOHelper.exists(sentryFile)) {
            try {
                logOnDetails.setSentryFileHash(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA1, IOHelper.read(sentryFile)));
            } catch (IOException e) {
                log.error("Can't read sentry file: {}", sentryFile.getFileName());
                log.error("Read sentry file exception", e);
            }
        } else {
            logOnDetails.setSentryFileHash(null);
        }
        logOnDetails.setShouldRememberPassword(true);

        steamUser.logOn(logOnDetails);
    }

    /**
     * Checks if sessionId and token cookies are still valid.
     * Sets cookie flag if they are invalid.
     *
     * @return true if cookies are valid
     */
    private boolean checkCookies() {
        // We still haven't re-authenticated
        if (cookiesAreInvalid) {
            return false;
        }

        final List<Cookie> cookies = steamWeb.getCookies();
        for (Cookie cookie : cookies) {
            log.debug("Cookie: {}, {}, {}", cookie.getName(), cookie.getValue(), cookie.getExpiryDate());
        }

        if (!steamWeb.verifyCookies()) {
            // Cookies are no longer valid
            log.warn("Cookies are invalid. Need to re-authenticate.");
            cookiesAreInvalid = true;
            requestWepApiAuth();
            return false;
        }

        return true;
    }

    private void requestWepApiAuth() {
        steamUser.requestWebAPIUserNonce().getResult(callback -> {
            log.debug("Received new WebAPIUserNonce.");
            if (callback.getResult() == EResult.OK) {
                myUserNonce = callback.getNonce();
                userWebLogOn();
            } else {
                log.error("WebAPIUserNonce Error: " + callback.getResult());
            }
        });
    }

    private void initTradeManager() {
        tradeManager = new TradeManager(botConfig.getApiKey(), steamWeb);
        tradeManager.setTradeTimeLimits(botConfig.getMaxTradeTime(), botConfig.getMaxActionGap(), botConfig.getTradePoolingInterval());
        tradeManager.getOnTimeout().addEventListener(args -> onTradeTimeout());

        tradeOfferManager = new TradeOfferManager(botConfig.getApiKey(), steamWeb);
        subscribeTradeOffer(tradeOfferManager);

        // Success, check trade offers which we have received while we were offline
        spawnTradeOfferPollingThread();
    }

    private void spawnTradeOfferPollingThread() {
        if (tradeOfferThread == null) {
            isTradeOfferActive = true;
            tradeOfferThread = CommonHelper.newThread("TradeOffer Polling Thread", true, () -> {
                while (isTradeOfferActive) {
                    try {
                        tradeOfferManager.enqueueUpdatedOffers();
                    } catch (Exception e) {
                        log.error("Error while polling trade offers", e);
                    }
                    ConcurrentHelper.sleep(botConfig.getTradeOfferPollingIntervalSecs());
                }
            });
            tradeOfferThread.start();
        }
    }

    private void cancelTradeOfferPollingThread() {
        if (tradeOfferThread != null) {
            isTradeOfferActive = false;
            tradeOfferThread = null;
        }
    }

    private void onTradeTimeout() {
        getUserHandler(currentTrade.getOtherSID()).onTradeTimeout();
    }

    private boolean handleTradeSessionStart(SteamID other) {
        if (currentTrade != null) {
            return false;
        }
        currentTrade = tradeManager.createTrade(steamUser.getSteamID(), other);
        currentTrade.getOnCloseEvent().addEventListener(args -> closeTrade());
        subscribeTrade(currentTrade, getUserHandler(other));
        tradeManager.startTrade(currentTrade);

        return true;
    }

    private UserHandler getUserHandler(SteamID sid) {
        log.debug("UserHandler size: {}", userHandlers.size());
        if (!userHandlers.containsKey(sid)) {
            userHandlers.put(sid, createHandler(this, sid));
        }
        return userHandlers.get(sid);
    }

    private void removeUserHandler(SteamID sid) {
        userHandlers.remove(sid);
    }

    /**
     * A method to return an instance of the <c>BotControlClass</c>.
     *
     * @param bot The bot
     * @param sid The steamId
     * @return A UserHandler instance
     */
    private UserHandler createHandler(Bot bot, SteamID sid) {
        Class<?> controlClass = null;
        try {
            controlClass = Class.forName(bot.getBotConfig().getBotControlClass());
        } catch (ClassNotFoundException e) {
            log.warn("{} isn't in classpath, using SimpleUserHandler", bot.getBotConfig().getBotControlClass());
            return new SimpleUserHandler(bot, sid);
        }

        UserHandler instance = null;
        try {
            Constructor<?> constructor = controlClass.getConstructor(Bot.class, SteamID.class);
            instance = (UserHandler) constructor.newInstance(bot, sid);
        } catch (Exception e) {
            log.error("Construct new handler exception", e);
        }

        return instance;
    }

    private String readLine() {
        try {
            final String input = JaSteamServer.COMMAND_HANDLER.readLine();
            log.debug("Entered: {}", input);
            return input;
        } catch (IOException e) {
            log.error("Console readLine exception", e);
            return "";
        }
    }

    public SteamWeb getSteamWeb() {
        return steamWeb;
    }

    public BotConfig getBotConfig() {
        return botConfig;
    }

    public SteamFriends getSteamFriends() {
        return steamFriends;
    }

    public SteamClient getSteamClient() {
        return steamClient;
    }

    public SteamTrading getSteamTrade() {
        return steamTrade;
    }

    public SteamGameCoordinator getSteamGC() {
        return steamGC;
    }

    public SteamUser getSteamUser() {
        return steamUser;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }

    public Trade getCurrentTrade() {
        return currentTrade;
    }

    public Logger getLog() {
        return log;
    }

    public SteamGuardAccount getSteamGuardAccount() {
        return steamGuardAccount;
    }

    public List<Friend> getFriendList() {
        return Collections.unmodifiableList(friendList);
    }

    public Event<Void> getOnCloseEvent() {
        return onCloseEvent;
    }

    public SteamCommunity getSteamCommunity() {
        return steamCommunity;
    }

    public SteamWebApi getSteamWebApi() {
        return steamWebApi;
    }

    public SteamUnifiedMessages getSteamUnifiedMessages() {
        return steamUnifiedMessages;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bot bot = (Bot) o;
        return Objects.equals(botConfig, bot.botConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(botConfig);
    }
}
