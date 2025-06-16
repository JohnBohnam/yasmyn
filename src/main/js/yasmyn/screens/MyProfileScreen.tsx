import AsyncStorage from "@react-native-async-storage/async-storage";
import React, {useEffect, useMemo, useState} from "react";
import {
    View,
    Text,
    TextInput,
    Button,
    FlatList,
    Image,
    StyleSheet,
    Alert,
} from "react-native";
import {User} from "../Model";
import {API_BASE_URL} from "../constants";

const API_URL = API_BASE_URL;

const MyProfile: React.FC = () => {
    const [observed, setObserved] = useState<User[]>([]);
    const [observing, setObserving] = useState<User[]>([]);
    const [newFriendId, setNewFriendId] = useState("");
    const [myInfo, setMyInfo] = useState<User | null>(null);
    const [searchResults, setSearchResults] = useState<User[]>([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [isSearching, setIsSearching] = useState(false);

    useEffect(() => {
        const delayDebounce = setTimeout(() => {
            if (searchQuery.trim() === "") {
                setSearchResults([]);
                return;
            }

            const fetchSearchResults = async () => {
                const authToken = await AsyncStorage.getItem("authToken");
                if (!authToken) return;

                try {
                    setIsSearching(true);
                    const res = await fetch(`${API_URL}/users/search?username=${encodeURIComponent(searchQuery)}`, {
                        headers: {Authorization: `Bearer ${authToken}`},
                    });

                    if (!res.ok) throw new Error("Search failed");
                    const users: User[] = await res.json();
                    const myId = await AsyncStorage.getItem("userId");
                    const filteredUsers = users.filter(user => user.id.toString() !== myId);
                    setSearchResults(filteredUsers);
                } catch (error) {
                    console.error("Search error:", error);
                    setSearchResults([]);
                } finally {
                    setIsSearching(false);
                }
            };

            fetchSearchResults();
        }, 200);

        return () => clearTimeout(delayDebounce);
    }, [searchQuery]);


    const fetchMyInfo = async () => {
        const authToken = await AsyncStorage.getItem("authToken");
        if (!authToken) throw new Error("Authentication token is missing");

        try {
            const response = await fetch(`${API_URL}/me`, {
                method: "GET",
                headers: {Authorization: `Bearer ${authToken}`},
            });

            if (!response.ok) throw new Error("Failed to fetch user info");

            const userInfo: User = await response.json();
            setMyInfo(userInfo);
        } catch (error) {
            Alert.alert("Error", "Could not load user info");
        }
    };

    const fetchObserved = async () => {
        const authToken = await AsyncStorage.getItem("authToken");
        if (!authToken) throw new Error("Authentication token is missing");

        try {
            const [obsRes, ingRes] = await Promise.all([
                fetch(`${API_URL}/me/observed`, {
                    headers: {Authorization: `Bearer ${authToken}`},
                }),
                fetch(`${API_URL}/me/observing`, {
                    headers: {Authorization: `Bearer ${authToken}`},
                }),
            ]);

            if (!obsRes.ok || !ingRes.ok) throw new Error("Fetch failed");


            const observedUsers: User[] = await obsRes.json();
            const observingUsers: User[] = await ingRes.json();

            setObserved(observedUsers);
            setObserving(observingUsers);


        } catch (error) {
            Alert.alert("Error", "Could not load observe data");
        }
    };


    const observeUserById = async (id: number) => {
        try {
            const authToken = await AsyncStorage.getItem("authToken");
            if (!authToken) throw new Error("Authentication token is missing");

            const response = await fetch(`${API_URL}/me/observe`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${authToken}`,
                },
                body: JSON.stringify({observedUserId: id}),
            });

            if (response.status === 409) {
                Alert.alert("Already observing this user.");
            } else if (!response.ok) {
                throw new Error("Failed to observe user");
            }

            setSearchResults([]);
            setNewFriendId("");
            await fetchObserved();
            await fetchMyInfo();
        } catch (error) {
            Alert.alert("Error", "Could not add observed user");
        }
    };


    const removeObserved = async (id: number) => {
        try {
            const authToken = await AsyncStorage.getItem("authToken");
            if (!authToken) throw new Error("Authentication token is missing");

            await fetch(`${API_URL}/me/observe`, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${authToken}`,
                },
                body: JSON.stringify({observedUserId: id}),
            });

            await fetchObserved();
            await fetchMyInfo();
        } catch {
            Alert.alert("Error", "Could not remove observed user");
        }
    };

    useEffect(() => {
        fetchObserved();
        fetchMyInfo();
    }, []);


    if (!myInfo) {
        return (
            <View style={styles.container}>
                <Text>Loading profile...</Text>
            </View>
        );
    }
    
    let imagePrefix = 'http://localhost:8080/uploads/';


    return (
        <View style={styles.container}>
            {/* Profile Info */}
            <View style={styles.profileContainer}>
                <Image
                    source={{uri: imagePrefix + myInfo.imageUrl}}
                    style={styles.profileImage}
                />
                <Text style={styles.profileName}>{myInfo.username}</Text>
                <Text>
                    You observe {observed.length} {observed.length === 1 ? "user" : "users"}.
                </Text>
                <Text>
                    {observing.length} {observing.length === 1 ? "user observes you." : "users observe you."}
                </Text>
            </View>

            {/* Observed List */}
            <View style={styles.section}>
                <Text style={styles.sectionTitle}>Observed</Text>
                <FlatList
                    data={observed}
                    extraData={observed}
                    keyExtractor={(item) => item.id.toString()}
                    renderItem={({ item }) => (
                    <View style={styles.friendRow}>
                        <View style={styles.userInfo}>
                            <Image
                                source={{ uri: imagePrefix + item.imageUrl }}
                                style={styles.friendImage}
                            />
                            <Text style={{ marginLeft: 8 }}>{item.username}</Text>
                        </View>
                        <Button
                            title="Remove"
                            onPress={() => removeObserved(item.id)}
                            color="red"
                        />
                    </View>
                )}
                />
            </View>

            <View style={styles.section}>
                <Text style={styles.sectionTitle}>Search Users by Username</Text>
                <TextInput
                    style={styles.input}
                    value={searchQuery}
                    onChangeText={setSearchQuery}
                    placeholder="Start typing username..."
                />
                {isSearching && <Text>Searching...</Text>}
                {searchResults.length > 0 && (
                    <FlatList
                        data={searchResults}
                        keyExtractor={(item) => item.id.toString()}
                        renderItem={({ item }) => (
                        <View style={styles.friendRow}>
                            <View style={styles.userInfo}>
                                <Image
                                    source={{ uri: imagePrefix + item.imageUrl }}
                                    style={styles.friendImage}
                                />
                                <Text style={{ marginLeft: 8 }}>{item.username}</Text>
                            </View>
                            <Button title="Observe" onPress={() => observeUserById(item.id)} />
                        </View>
                    )}
                    />
                )}
            </View>


            {/* Observing List */}
            <View style={styles.section}>
                <Text style={styles.sectionTitle}>Users Observing You</Text>
                <FlatList
                    data={observing}
                    extraData={observing}
                    keyExtractor={(item) => item.id.toString()}
                    renderItem={({ item }) => (
                    <View style={styles.friendRow}>
                        <View style={styles.userInfo}>
                            <Image
                                source={{ uri: imagePrefix + item.imageUrl }}
                                style={styles.friendImage}
                            />
                            <Text style={{ marginLeft: 8 }}>{item.username}</Text>
                        </View>
                    </View>
                )}
                />
            </View>
        </View>
    );
};

export default MyProfile;

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 20,
    },
    profileContainer: {
        alignItems: "center",
        marginBottom: 24,
    },
    profileImage: {
        width: 100,
        height: 100,
        borderRadius: 50,
        marginBottom: 8,
    },
    profileName: {
        fontSize: 20,
        fontWeight: "bold",
        marginBottom: 4,
    },
    section: {
        marginBottom: 24,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: "600",
        marginBottom: 8,
    },
    input: {
        borderWidth: 1,
        borderColor: "#ccc",
        borderRadius: 6,
        padding: 8,
        marginBottom: 10,
    },
    friendRow: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "flex-start",
        paddingVertical: 6,
    },
    friendImage: {
        width: 30,
        height: 30,
        borderRadius: 15,
    },
    userInfo: {
        flexDirection: 'row',
        alignItems: 'center',
        flexShrink: 1,  // so username text doesn't push buttons off screen
    },
});
