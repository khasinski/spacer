package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.renderer.RenderManager;
import java.io.IOException;
import java.util.Date;
import spacers.Spacers;
import spacers.message.MessageMob;
import spacers.message.MessagePlayerSpeed;
import spacers.message.MessageWelcome;

public class Main extends SimpleApplication {

    public static ClientMob me;
    private static Client client;

    public static void main(String[] args) throws IOException {
        Spacers.initializeClasses();

        Main app = new Main();

        client = Network.connectToServer(Spacers.NAME, Spacers.VERSION,
                Spacers.HOST, Spacers.PORT, Spacers.UDP_PORT);
        client.addMessageListener(new ChatHandler(), MessageMob.class);
        client.addMessageListener(new MessageListener<Client>() {
            public void messageReceived(Client source, Message m) {
                me = ClientMob.mobs.get(((MessageWelcome) m).mob);
            }
        }, MessageWelcome.class);

        app.start();
    }

    @Override
    public void simpleInitApp() {
        ClientMob.callback = new ClientMob.MobInterface() {
            public void onCreate(ClientMob m) {
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                switch (m.type) {
                    case CHECKPOINT:
                        mat.setColor("Color", ColorRGBA.Blue);
                        break;
                    case PLAYER:
                        mat.setColor("Color", ColorRGBA.Red);
                        break;
                }
                m.geometry.setMaterial(mat);
                rootNode.attachChild(m.geometry);
            }
        };

        client.start();
    }

    @Override
    public void simpleUpdate(float tpf) {
        final float t = (new Date().getTime() - ClientMob.ts) / (1000.f / Spacers.TICKS);

        Vector3f speed = new Vector3f(0, 0, 0.2f);
        client.send(new MessagePlayerSpeed(speed));

        final Vector3f p = me.position.add(speed.mult(t));
        getCamera().lookAt(p, new Vector3f(0.f, 1.f, 0.f));
        getCamera().setLocation(p.add(new Vector3f(0, 0, -10f)));

        for (ClientMob c : ClientMob.mobs) {
            c.geometry.setLocalTranslation(c.position.add(c.speed.mult(t)));
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    private static class ChatHandler implements MessageListener<Client> {

        public void messageReceived(Client source, Message m) {
            MessageMob chat = (MessageMob) m;
            ClientMob.fromMessage(chat);
        }
    }

    @Override
    public void destroy() {
        client.close();

        super.destroy();
    }
}
