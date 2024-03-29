package org.energy.abacus.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@NamedQuery(name = "findFriendshipByUsers", query = "SELECT f FROM Friendship f WHERE (f.requestReceiverId = :id OR f.requestSenderId = :id) AND (f.requestReceiverId = :friendId OR f.requestSenderId = :friendId)")
@NamedQuery(name = "updateFriendshipByUsers", query = "UPDATE Friendship SET accepted = :reaction WHERE requestReceiverId = :receiver AND requestSenderId = :sender")
@NamedQuery(name = "deleteFriendshipByUsers", query = "DELETE FROM Friendship WHERE requestReceiverId = :receiver AND requestSenderId = :sender")
@NamedQuery(name = "findFriendshipUsers", query = "SELECT f FROM Friendship f WHERE f.requestReceiverId = :id OR f.requestSenderId = :id")
@NamedQuery(name = "deleteFriendshipByIdAndUser", query = "DELETE FROM Friendship WHERE id = :id AND (requestReceiverId = :userId OR requestSenderId = :userId)")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String requestSenderId;

    @Column(nullable = false)
    private String requestReceiverId;


    boolean accepted;
}
